/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestorftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author Pablo Quesada
 */
public class Descifrador {
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
                System.err.println("Error, no se pudo conectar al servidor.");
                return;
            }
            
            System.out.println("Conectado al servidor FTP correctamente.");
            
            System.out.print("Ruta del archivo en el servidor FTP: ");
            String rutaRemota = scanner.nextLine();
            
            System.out.print("Ruta local para guardado: ");
            String rutaLocal = scanner.nextLine();
            
            if (!clienteFTP.descargarArchivo(rutaRemota, rutaLocal)) {
                System.err.println("No se pudo descargar el archivo.");
                clienteFTP.desconectar();
                return;
            }
            
            System.out.print("¿El archivo está cifrado? (s/n): ");
            String respuesta = scanner.nextLine();
            
            if (respuesta.equalsIgnoreCase("s")) {
                System.out.print("Clave de cifrado AES: ");
                String claveAES = scanner.nextLine();
                
                Cifrador cifrador = new Cifrador(claveAES);
                
                String contenidoCifrado = new String(Files.readAllBytes(Paths.get(rutaLocal)));
                
                String contenidoDescifrado = cifrador.descifrar(contenidoCifrado);
                
                if (contenidoDescifrado != null) {
                    String rutaDescifrada = rutaLocal + ".descifrado";
                    Files.write(Paths.get(rutaDescifrada), contenidoDescifrado.getBytes());
                    System.out.println("Archivo descifrado ruta: " + rutaDescifrada);
                } else {
                    System.err.println("Error");
                }
            }
            
            clienteFTP.desconectar();
            System.out.println("Operación completada.");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
