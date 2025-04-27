package Cliente;

import java.io.*;
import java.net.Socket;

public class ClienteConcurrente extends Thread {

    private final String host;
    private final int puerto;

    public ClienteConcurrente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        Socket socket = null;
        DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        BufferedReader entrada = null;

        try {
            socket = new Socket(host, puerto);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            entrada = new BufferedReader(new InputStreamReader(System.in));

            // Llama directamente al método procesarConcurrente
            ProtocoloCliente.procesarConcurrente(entrada, dataIn, dataOut, socket);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (entrada != null) entrada.close();
                if (dataIn != null) dataIn.close();
                if (dataOut != null) dataOut.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
