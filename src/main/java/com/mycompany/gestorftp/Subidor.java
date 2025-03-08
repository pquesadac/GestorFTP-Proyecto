/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestorftp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author Pablo Quesad Castellano
 */
public class Subidor {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.print("Servidor FTP: ");
            String servidor = scanner.nextLine();
            
            System.out.print("Puerto FTP (21): ");
            String puertotmp = scanner.nextLine();
            int puerto = puertotmp.isEmpty() ? 21 : Integer.parseInt(puertotmp);
            
            System.out.print("Usuario FTP: ");
            String usuario = scanner.nextLine();
            
            System.out.print("Contraseña FTP: ");
            String password = scanner.nextLine();
            
            ClienteFTP clienteFTP = new ClienteFTP(servidor, puerto, usuario, password);
            
            if (!clienteFTP.conectar()) {
                System.err.println("Error, no se pudo conectar al servidor FTP.");
                return;
            }
            
            System.out.println("Conectado al servidor FTP correctamente.");
            
            System.out.print("Ruta del archivo local a subir: ");
            String rutaLocal = scanner.nextLine();
            
            File archivo = new File(rutaLocal);
            if (!archivo.exists()) {
                System.err.println("Error: El archivo no existe");
                clienteFTP.desconectar();
                return;
            }
            
            if (!archivo.canRead()) {
                System.err.println("Error: No se puede leer el archivo.");
                clienteFTP.desconectar();
                return;
            }
            
            System.out.println("Archivo encontrado y accesible: " + rutaLocal);
            System.out.println("Tamaño del archivo: " + archivo.length() + " bytes");
            
            System.out.print("Carpeta remota en el servidor FTP: ");
            String carpetaRemota = scanner.nextLine();
            
            Path path = Paths.get(rutaLocal);
            String nombreArchivo = path.getFileName().toString();
            String rutaRemota = carpetaRemota + "/" + nombreArchivo;
            
            System.out.print("Clave de cifrado AES: ");
            String claveAES = scanner.nextLine();
            
            boolean esTexto = esArchivoTexto(nombreArchivo);
            
            if (esTexto) {
                System.out.println("Es archivo de texto, se cifrará antes de subir...");
                try {
                    Cifrador cifrador = new Cifrador(claveAES);
                    String contenido = new String(Files.readAllBytes(path));
                    System.out.println("Contenido leído correctamente, longitud: " + contenido.length() + " caracteres");
                    
                    String contenidoCifrado = cifrador.cifrar(contenido);
                    System.out.println("Contenido cifrado correctamente");
                    
                    Path archivoCifrado = Files.createTempFile("cifrado_", ".tmp");
                    Files.write(archivoCifrado, contenidoCifrado.getBytes());
                    System.out.println("Archivo temporal creado: " + archivoCifrado);
                    
                    boolean resultado = clienteFTP.subirArchivo(archivoCifrado.toString(), rutaRemota);
                    if (resultado) {
                        System.out.println("Archivo cifrado y subido correctamente a: " + rutaRemota);
                    }
                    
                    Files.delete(archivoCifrado);
                } catch (IOException e) {
                    System.err.println("Error detallado: " + e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                boolean resultado = clienteFTP.subirArchivo(rutaLocal, rutaRemota);
                if (resultado) {
                    System.out.println("Archivo subido correctamente a: " + rutaRemota);
                }
            }
            
            clienteFTP.desconectar();
            System.out.println("Operación completada. Ahora puedes usar el Descifrador para recuperar el archivo.");
            
        } catch (Exception e) {
            System.err.println("Error general: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static boolean esArchivoTexto(String nombreArchivo) {
        String[] extensionesTexto = {".txt", ".html", ".xml", ".json", ".csv", ".log", ".md", ".java", ".py", ".js", ".css"};
        for (String ext : extensionesTexto) {
            if (nombreArchivo.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
