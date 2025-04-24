package Servidor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import Cifrados.Asimetrico;


public class ProtocoloServidor {
    //Boolean para matar el ciclo del servidor
    public static void procesar(BufferedReader pIn, PrintWriter pOut, Socket socket) throws IOException {

        String inputLine = pIn.readLine();
        System.out.println("Entrada a procesar: " + inputLine);
        String reto = pIn.readLine();
        PrivateKey llavePrivada = cargarLlavePriv();
        // Cifrar el reto con la llave privada
        byte[] textoCifrado = Asimetrico.cifrar(llavePrivada, "RSA", reto); 

        OutputStream out = socket.getOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(textoCifrado.length); 
        dataOut.write(textoCifrado);           
        dataOut.flush();
    }


    public static PrivateKey cargarLlavePriv() throws IOException{
        FileInputStream archivoPuvKey = new FileInputStream("src/llaves/llave_privada.key");
        ObjectInputStream publicKey = new ObjectInputStream(archivoPuvKey);
        try {
            PrivateKey llavePrivada = (PrivateKey) publicKey.readObject();
            publicKey.close();
            return llavePrivada;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey cargarLlavePub() throws IOException{
        FileInputStream archivoPubKey = new FileInputStream("src/llaves/llave_publica.key");
        ObjectInputStream publicKey = new ObjectInputStream(archivoPubKey);
        try {
            PublicKey llavePublica = (PublicKey) publicKey.readObject();
            publicKey.close();
            return llavePublica;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    
}
