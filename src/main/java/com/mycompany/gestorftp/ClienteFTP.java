/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestorftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.io.*;

/**
 *
 * @author Pablo Quesada
 */
public class ClienteFTP {

    private String servidor;
    private int puerto;
    private String usuario;
    private String password;
    private FTPClient cliente;

    public ClienteFTP(String servidor, int puerto, String usuario, String password) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.usuario = usuario;
        this.password = password;
        this.cliente = new FTPClient();
    }

    /*
      Intenta conectar al servidor FTP con los datos proporcionados.
     
      @return true si la conexión y autenticación fueron exitosas, false en caso contrario.
      @throws IOException si ocurre un error durante la conexión.
     */
    public boolean conectar() throws IOException {
        cliente.connect(servidor, puerto);

        // Verificamos si la conexión  es exitosa
        int respuesta = cliente.getReplyCode();
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            cliente.disconnect();
            System.err.println("Se rechazó la conexión.");
            return false;
        }

        // Intentamos conectarnos con el usuario y la contraseña 
        if (!cliente.login(usuario, password)) {
            cliente.disconnect();
            System.err.println("No se pudo iniciar sesión");
            return false;
        }

        // Establecemos el tipo de transferencia a binaria para asi evitar problemas con archivos
        cliente.setFileType(FTPClient.BINARY_FILE_TYPE);
        // Activamos el modo pasivo
        cliente.enterLocalPassiveMode();

        //Si todo va bien devuelve true
        return true;
    }

    /*
      Cierra la sesión y desconecta del servidor FTP si la conexión está activa.
     
      @throws IOException si ocurre un error al desconectar.
     */
    public void desconectar() throws IOException {
        if (cliente.isConnected()) {
            cliente.logout();
            cliente.disconnect();
        }
    }

    // Método para subir un archivo al servidor FTP
    public boolean subirArchivo(String local, String ruta) throws IOException {
        // Creamos un objeto File
        File localFile = new File(local);
        FileInputStream inputStream = new FileInputStream(localFile);

        try {
            System.out.println("Subiendo archivo: " + local + " a " + ruta);
            // Intentamos almacenar el archivo en el servidor FTP
            boolean resultado = cliente.storeFile(ruta, inputStream);
            if (resultado) {
                System.out.println("Archivo subido correctamente");
            } else {
                System.err.println("Error, no se pudo subir el archivo");
            }
            return resultado;
        } finally {
            inputStream.close();
        }
    }

// Método para descargar un archivo del servidor FTP
    public boolean descargarArchivo(String rutaRemota, String archivoLocal) throws IOException {
        // Creamos un objeto File
        File localFile = new File(archivoLocal);
        FileOutputStream outputStream = new FileOutputStream(localFile);

        try {
            System.out.println("Descargando archivo: " + rutaRemota + " a " + archivoLocal);
            // Intentamos recuperar el archivo desde el servidor FTP

            boolean resultado = cliente.retrieveFile(rutaRemota, outputStream);
            if (resultado) {
                System.out.println("Archivo descargado correctamente");
            } else {
                System.err.println("Error, no se pudo descargar el archivo");
            }
            return resultado;
        } finally {
            outputStream.close();
        }
    }

    public boolean eliminarArchivo(String rutaRemota) throws IOException {
        System.out.println("Eliminando archivo: " + rutaRemota);
        boolean resultado = cliente.deleteFile(rutaRemota);
        if (resultado) {
            System.out.println("Archivo eliminado");
        } else {
            System.err.println("Error, no se pudo eliminar el archivo");
        }
        return resultado;
    }

    public boolean comprobacion(String ruta) throws IOException {
        String[] archivos = cliente.listNames(ruta);
        return archivos != null && archivos.length > 0;
    }

    public boolean crearDirectorio(String directorio) throws IOException {
        return cliente.makeDirectory(directorio);
    }

    public boolean cambiarDirectorio(String directorio) throws IOException {
        return cliente.changeWorkingDirectory(directorio);
    }

}
