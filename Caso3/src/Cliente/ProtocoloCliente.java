package Cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

import Cifrados.Asimetrico;

public class ProtocoloCliente {
    public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, Socket socket) throws IOException{

        //Lee el teclado
        System.out.println("Escriba el mensaje a enviar al servidor: ");
        String fromUser = stdIn.readLine();
        pOut.println(fromUser);

        System.out.println("Ingrese el numero que servira como modulo: ");
        int modulo = Integer.parseInt(stdIn.readLine());
        System.out.println("Ingrese el numero que servira como multiplicador: ");
        int multiplicador = Integer.parseInt(stdIn.readLine());
        System.out.println("Ingrese el numero que servira como incremento: ");
        int incremento = Integer.parseInt(stdIn.readLine());
        System.out.println("Ingrese el numero que servira como limite, por favor que este numero sea mayor o igual a 7: ");
        int limite = Integer.parseInt(stdIn.readLine());
        System.out.println("Ingrese el numero inicial de la secuencia: ");
        int semilla = Integer.parseInt(stdIn.readLine());

        // Crear el reto      
        String reto = reto(semilla, multiplicador, incremento, modulo, limite, new int[limite + 1]);  
        pOut.println(reto); 

        PublicKey llavePublica = cargarLlavePub();

        DataInputStream in = new DataInputStream(socket.getInputStream());
        int longitud = in.readInt(); 
        byte[] textoCifrado = new byte[longitud];
        in.readFully(textoCifrado); 

        byte[] textoDescifrado = Asimetrico.descifrar(llavePublica, "RSA", textoCifrado);
        String textoClaro = new String(textoDescifrado);
        
        if (textoClaro.equals(reto)) {
            pOut.println("OK");
        } else {
            System.out.println("El reto fue incorrecto: " + textoClaro);
            pOut.println("ERROR");
        }
    }

    public static String reto(int semilla, int multiplicador, int incremento, int modulo, int limite, int[] numeros) {
        String reto = "";
        reto += String.valueOf(semilla);
        numeros[0] = semilla;

        for (int i = 1; i <= limite; i++) {
            numeros[i] = (multiplicador * numeros[i - 1] + incremento) % modulo;
            reto += String.valueOf(numeros[i]);
        }
        return reto;
    }

    public static PublicKey cargarLlavePub() throws IOException{
        FileInputStream archivoPuvKey = new FileInputStream("src/llaves/llave_publica.key");
        ObjectInputStream publicKey = new ObjectInputStream(archivoPuvKey);
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
