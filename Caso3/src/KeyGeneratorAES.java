import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class KeyGeneratorAES {
    private final static String ALGORITMO = "AES";

    public static void main(String[] args) {
        try {
            // Generar llave secreta AES
            KeyGenerator keygen = KeyGenerator.getInstance(ALGORITMO);
            SecretKey llave = keygen.generateKey();

            // Guardar la llave en un archivo
            FileOutputStream archivoLlave = new FileOutputStream("llave.secreta");
            ObjectOutputStream oosLlave = new ObjectOutputStream(archivoLlave);
            oosLlave.writeObject(llave);
            oosLlave.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
