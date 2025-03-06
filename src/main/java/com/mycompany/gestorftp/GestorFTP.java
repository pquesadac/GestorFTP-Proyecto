/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.gestorftp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pablo Quesada
 */
public class GestorFTP {

    public StringBuilder getContenidoMetodoGet(String direccion) throws Exception {
        System.out.println("Descargando " + direccion);
        StringBuilder respuesta = new StringBuilder();
        URL url = new URL(direccion);
        HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
        conexion.setRequestMethod("GET");
        conexion.setRequestProperty("Content-Type", "text/plain");
        conexion.setRequestProperty("charset", "utf-8");
        conexion.setRequestProperty("User-Agent", "Mozilla/5.0");
        int estado = conexion.getResponseCode();
        System.out.println("Código de respuesta: " + estado);

        Reader streamReader = null;
        if (estado == HttpURLConnection.HTTP_OK) {

            System.out.print("Descargando Contenido...");
            streamReader = new InputStreamReader(conexion.getInputStream());
            int caracter;
            while ((caracter = streamReader.read()) != -1) {
                respuesta.append((char) caracter);
            }
            System.out.println("terminado");
        } else {
            //Si no se recibe el 200 OK se muestra la cabecera
            Map<String, List<String>> redireccion = conexion.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : redireccion.entrySet()) {
                System.out.println("Key = " + entry.getKey()
                        + ", Value = " + entry.getValue());
            }
            throw new Exception("Error HTTP " + estado);
        }
        conexion.disconnect();
        return respuesta;
    }

    public static void writeFile(String strPath, String contenido) throws IOException {
        System.out.println("Guardando recurso: " + strPath);
        Path path = Paths.get(strPath);
        byte[] strToBytes = contenido.getBytes();
        Files.write(path, strToBytes);
        System.out.println("Recurso Guadado.");
    }

    public static void main(String[] args) {
        try {
            String termino = "perro";
            String esquema = "https://";
            String servidor = "www.imdb.com/";
            String path = "/find/";
            String parametros = "?q=" + URLEncoder.encode(termino, StandardCharsets.UTF_8.name());

            GestorFTP peticiones = new GestorFTP();

            String direccion = esquema + servidor + path + parametros;

            StringBuilder resultado = peticiones.getContenidoMetodoGet(direccion);
            GestorFTP.writeFile("D:/Descargas/peli_" + termino + ".html", resultado.toString());

            System.out.println("Descarga finalizada");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
