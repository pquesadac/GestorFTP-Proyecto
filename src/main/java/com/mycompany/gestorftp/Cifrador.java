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
 *
 * @author Pablo Quesada
 */
public class Cifrador {
 private SecretKeySpec claveSecreta;


    public Cifrador(String clave) {
        try {
            byte[] claveBytes = clave.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            claveBytes = sha.digest(claveBytes);
            claveBytes = Arrays.copyOf(claveBytes, 16); 
            claveSecreta = new SecretKeySpec(claveBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    public String cifrar(String texto) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta);
            byte[] textoCifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(textoCifrado);
        } catch (Exception e) {
            System.err.println("Error al cifrar: " + e.getMessage());
            return null;
        }
    }


    public String descifrar(String textoCifrado) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta);
            byte[] bytes = Base64.getDecoder().decode(textoCifrado);
            byte[] textoDescifrado = cipher.doFinal(bytes);
            return new String(textoDescifrado, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error al descifrar: " + e.getMessage());
            return null;
        }
    }
}
