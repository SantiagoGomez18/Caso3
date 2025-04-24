package Cliente;

import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
public class Cliente {
    public static final int PUERTO = 8080;
    public static final String HOST = "localhost";

    public static void main(String args[]) throws IOException {
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;

        System.out.println("Cliente ...");

        try{
            // Crear el socket en el lado cliente
            socket = new Socket(HOST, PUERTO);
            escritor = new PrintWriter(socket.getOutputStream(), true);
            lector = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }

        BufferedReader entrada = new BufferedReader(new java.io.InputStreamReader(System.in));
        
        // Pasan cositas
        ProtocoloCliente.procesar(entrada, lector, escritor, socket);

        // se cierran los flujos y el socket
        entrada.close();    
        escritor.close();
        lector.close();
        socket.close();
    }

}
