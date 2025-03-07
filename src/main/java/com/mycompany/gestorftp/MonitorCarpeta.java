/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestorftp;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Pablo Quesada
 */
public class MonitorCarpeta {

    private final Path carpetaLocal;
    private final String carpetaRemota;
    private final ClienteFTP clienteFTP;
    private final Cifrador cifrador;
    private final WatchService watchService;
    private final Map<Path, Long> archivosRegistrados = new HashMap<>();
    private final ExecutorService pool;
    private volatile boolean ejecutando = true;

    public MonitorCarpeta(String carpetaLocal, String carpetaRemota, ClienteFTP clienteFTP, String claveAES) throws IOException {
        this.carpetaLocal = Paths.get(carpetaLocal);
        this.carpetaRemota = carpetaRemota;
        this.clienteFTP = clienteFTP;
        this.cifrador = new Cifrador(claveAES);
        this.watchService = FileSystems.getDefault().newWatchService();
        this.pool = Executors.newFixedThreadPool(5); 

        this.carpetaLocal.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        añadirArchivoExist();
    }

    /**
     * Agregamos todos los archivos existentes en la carpeta local
     */
    private void añadirArchivoExist() throws IOException {
        Files.walkFileTree(carpetaLocal, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                archivosRegistrados.put(file, attrs.lastModifiedTime().toMillis());
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Inicia el monitoreo de la carpeta
     */
    public void iniciarMonitoreo() {
        // Primero subimos todos los archivos existentes
        sincronizarArchivos();

        // Luego iniciamos el monitoreo continuo
        Thread monitorThread = new Thread(() -> {
            try {
                while (ejecutando) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        // Ignoramos eventos de desbordamiento
                        if (kind == OVERFLOW) {
                            continue;
                        }

                        // Se obtiene el nombre del archivo afectado
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();
                        Path completePath = carpetaLocal.resolve(fileName);

                        // Procesamos el evento en un hilo separado
                        procesarEvento(kind, completePath);
                    }

                    // Restablecemos la llave
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Monitor interrumpido: " + e.getMessage());
            }
        });

        monitorThread.start();
        System.out.println("Monitoreo iniciado en: " + carpetaLocal);
    }

    /**
     * Subimos todos los archivos existentes al inicio
     */
    private void sincronizarArchivos() {
        for (Path archivo : archivosRegistrados.keySet()) {
            pool.submit(() -> {
                try {
                    subirArchivo(archivo);
                } catch (IOException e) {
                    System.err.println("Error al subir archivo existente: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Procesamos un evento de archivo detectado
     */
    private void procesarEvento(WatchEvent.Kind<?> tipo, Path archivo) {
        pool.submit(() -> {
            try {
                if (Files.isDirectory(archivo)) {
                    return;
                }

                if (tipo == ENTRY_CREATE) {
                    System.out.println("Archivo creado: " + archivo);
                    subirArchivo(archivo);
                    archivosRegistrados.put(archivo, Files.getLastModifiedTime(archivo).toMillis());

                } else if (tipo == ENTRY_MODIFY) {
                    // Solo procesamos si realmente cambió (Asi evitamos eventos duplicados)
                    long nuevoTimestamp = Files.getLastModifiedTime(archivo).toMillis();
                    Long prevTimestamp = archivosRegistrados.get(archivo);

                    if (prevTimestamp == null || nuevoTimestamp > prevTimestamp) {
                        System.out.println("Archivo modificado: " + archivo);
                        subirArchivo(archivo);
                        archivosRegistrados.put(archivo, nuevoTimestamp);
                    }

                } else if (tipo == ENTRY_DELETE) {
                    System.out.println("Archivo eliminado: " + archivo);
                    eliminarArchivoRemoto(archivo);
                    archivosRegistrados.remove(archivo);
                }
            } catch (IOException e) {
                System.err.println("Error al procesar evento de archivo: " + e.getMessage());
            }
        });
    }

    /**
     * Subida de un archivo al servidor FTP
     */
    private void subirArchivo(Path archivo) throws IOException {
        // Obtenemos la ruta relativa
        Path relativePath = carpetaLocal.relativize(archivo);
        String ruta = carpetaRemota + "/" + relativePath.toString().replace("\\", "/");

        // Verificamos si es un archivo de texto plano para cifrarlo
        String nombreArchivo = archivo.toString();
        if (verificacion(nombreArchivo)) {
            String contenido = new String(Files.readAllBytes(archivo));
            String contenidoCifrado = cifrador.cifrar(contenido);

            // Creamos un archivo temporal cifrado
            Path archivoCifrado = Files.createTempFile("cifrado_", ".tmp");
            Files.write(archivoCifrado, contenidoCifrado.getBytes());

            try {
                clienteFTP.subirArchivo(archivoCifrado.toString(), ruta);
            } finally {
                // Eliminamos archivo temporal
                Files.delete(archivoCifrado);
            }
        } else {
            // Subimos archivo sin cifrar
            clienteFTP.subirArchivo(archivo.toString(), ruta);
        }
    }

    /**
     * Metodo que elimina un archivo del servidor FTP
     */
    private void eliminarArchivoRemoto(Path archivo) throws IOException {
        Path relativePath = carpetaLocal.relativize(archivo);
        String ruta = carpetaRemota + "/" + relativePath.toString().replace("\\", "/");
        clienteFTP.eliminarArchivo(ruta);
    }

    /**
     * Verificamos si un archivo es de texto plano
     */
    private boolean verificacion(String nombreArchivo) {
        String[] extensionesTexto = {".txt", ".html", ".xml", ".json", ".csv", ".log", ".md", ".java", ".py", ".js", ".css"};
        for (String ext : extensionesTexto) {
            if (nombreArchivo.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    
    public void detener() {
        ejecutando = false;
        pool.shutdown();
        try {
            watchService.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
