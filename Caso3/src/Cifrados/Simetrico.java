package Cifrados;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;

public class Simetrico {
    private final static String PADDING = "AES/CBC/PKCS5Padding";


    public static byte[] cifrar(SecretKey llave, byte[] iv, String texto) {
        try {
            Cipher cifrador = Cipher.getInstance(PADDING);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cifrador.init(Cipher.ENCRYPT_MODE, llave, ivSpec);
            return cifrador.doFinal(texto.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error al cifrar: " + e.getMessage());
            return null;
        }
    }


    public static String descifrar(SecretKey llave, byte[] iv, byte[] textoCifrado) {
        try {
            Cipher cifrador = Cipher.getInstance(PADDING);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cifrador.init(Cipher.DECRYPT_MODE, llave, ivSpec);
            byte[] textoClaro = cifrador.doFinal(textoCifrado);
            return new String(textoClaro, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Error al descifrar: " + e.getMessage());
            return null;
        }
    }
}