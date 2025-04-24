package Cifrados;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class Simetrico {
    private final static String PADDING = "AES/CBC/PKCS5Padding";

    // Método para cifrar
    public static byte[] cifrar(SecretKey llave, String texto) {
    byte[] textoCifrado;

        try {   
            Cipher cifrador = Cipher.getInstance(PADDING); 
            byte[] textoClaro = texto.getBytes();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            textoCifrado = cifrador.doFinal(textoClaro);

            return textoCifrado;
        } catch (Exception e) {
            System.out.println("Excepción: " + e.getMessage());
            return null;
        }
    }

    // Método para descifrar
    public static byte[] descifrar(SecretKey llave, byte[] texto) {
        byte[] textoClaro;

        try {
            Cipher cifrador = Cipher.getInstance(PADDING); 
            cifrador.init(Cipher.DECRYPT_MODE, llave);
            textoClaro = cifrador.doFinal(texto);
        } catch (Exception e) {
            System.out.println("Excepción: " + e.getMessage());
            return null;
        }

        return textoClaro;
    }
}

