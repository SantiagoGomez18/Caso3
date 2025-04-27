package Servidor;

import java.io.*;
import java.net.Socket;

public class ServidorDelegado extends Thread {
    private final Socket socket;
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;

    public ServidorDelegado(Socket socket, DataInputStream dataIn, DataOutputStream dataOut) {
        this.socket = socket;
        this.dataIn = dataIn;
        this.dataOut = dataOut;
    }

    @Override
    public void run() {
        try {
            System.out.println("Servidor delegado iniciado para cliente: " + socket.getRemoteSocketAddress());

            ProtocoloServidor.procesar(dataIn, dataOut, socket);

        } catch (IOException e) {
            System.err.println("Error en servidor delegado: " + e.getMessage());
        } finally {
            try {
                dataIn.close();
                dataOut.close();
                socket.close();
                System.out.println("Conexión con cliente cerrada.");
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
