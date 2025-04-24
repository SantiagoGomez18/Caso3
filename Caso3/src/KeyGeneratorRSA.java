import java.security.*;
import java.io.*;

public class KeyGeneratorRSA {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(1024);
        KeyPair claves = generador.generateKeyPair();

        // Guardar llave pública
        ObjectOutputStream pubKeyOOS = new ObjectOutputStream(new FileOutputStream("llave_publica.key"));
        pubKeyOOS.writeObject(claves.getPublic());
        pubKeyOOS.close();

        // Guardar llave privada
        ObjectOutputStream privKeyOOS = new ObjectOutputStream(new FileOutputStream("llave_privada.key"));
        privKeyOOS.writeObject(claves.getPrivate());
        privKeyOOS.close();

        System.out.println("Llaves RSA generadas.");
    }
}
