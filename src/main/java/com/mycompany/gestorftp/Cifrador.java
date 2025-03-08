/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestorftp;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Clase para cifrar y descifrar textos utilizando AES.
 * @author Pablo Quesada
 */
public class Cifrador {
    // Clave secreta utilizada para el cifrado y descifrado
    private SecretKeySpec claveSecreta;

    /**
     * Constructor que genera una clave secreta a partir de una cadena de texto.
     * @param clave La clave de entrada proporcionada por el usuario.
     */
    public Cifrador(String clave) {
        try {
            // Convertimos la clave en bytes usando UTF-8
            byte[] claveBytes = clave.getBytes(StandardCharsets.UTF_8);

            // Aplicamos SHA-1 para obtener un hash de la clave
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            claveBytes = sha.digest(claveBytes);
            claveBytes = Arrays.copyOf(claveBytes, 16);

            // Creamos la clave 
            claveSecreta = new SecretKeySpec(claveBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Método para cifrar un texto utilizando AES en modo ECB.
     * @return El texto cifrado en Base64 o null si ocurre un error.
     */
    public String cifrar(String texto) {
        try {
            // Creamos un objeto Cipher configurado para AES/ECB/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // Inicializamos el cifrador en modo ENCRYPT_MODE con la clave secreta
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta);

            // Ciframos el texto y obtenemos los bytes cifrados
            byte[] textoCifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            // Convertimos los bytes cifrados en una cadena Base64 para facilitar su almacenamiento y transmisión
            return Base64.getEncoder().encodeToString(textoCifrado);
        } catch (Exception e) {
            System.err.println("Error al cifrar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método para descifrar un texto cifrado en Base64 utilizando AES en modo ECB.
     * @return El texto en claro descifrado o null si ocurre un error.
     */
    public String descifrar(String textoCifrado) {
        try {
            // Creamos un objeto Cipher configurado para AES/ECB/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // Inicializamos el cifrador en modo DECRYPT_MODE con la clave secreta
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta);

            // Decodificamos el texto desde Base64 a un array de bytes cifrados
            byte[] bytes = Base64.getDecoder().decode(textoCifrado);

            // Desciframos los bytes cifrados para obtener el texto original
            byte[] textoDescifrado = cipher.doFinal(bytes);

            // Convertimos los bytes descifrados en una cadena de texto UTF-8
            return new String(textoDescifrado, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error al descifrar: " + e.getMessage());
            return null;
        }
    }
}
