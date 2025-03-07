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
public class Main {
    
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
                System.err.println("Error, no se pudo conectar al servidor");
                return;
            }
            
            System.out.println("Conectado al servidor FTP correctamente.");
            
            System.out.print("Carpeta local a sincronizar: ");
            String carpetaLocal = scanner.nextLine();
            
            if (!Files.exists(Paths.get(carpetaLocal))) {
                System.err.println("La carpeta local no existe.");
                clienteFTP.desconectar();
                return;
            }
            
            System.out.print("Carpeta remota en el servidor FTP: ");
            String carpetaRemota = scanner.nextLine();
            
            clienteFTP.crearDirectorio(carpetaRemota);
            
            System.out.print("Clave de cifrado AES: ");
            String claveAES = scanner.nextLine();
            
            MonitorCarpeta monitor = new MonitorCarpeta(carpetaLocal, carpetaRemota, clienteFTP, claveAES);
            monitor.iniciarMonitoreo();
            
            System.out.println("\nServicio iniciado. Presione ENTER para finalizar.");
            scanner.nextLine();
            
            monitor.detener();
            clienteFTP.desconectar();
            System.out.println("Servicio detenido correctamente.");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}