package Servidor;

import java.io.*;
import java.net.*;

public class Servidor {
    public static final int PUERTO = 8080;

    public static void main(String args[]) throws IOException {
        ServerSocket ss = null;
        boolean continuar = true;

        System.out.println("Main Server iniciado...");

        try {
            ss = new ServerSocket(PUERTO);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while (continuar) {
            try {
                Socket socket = ss.accept();
                System.out.println("Nueva conexión aceptada. Creando servidor delegado...");

                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

                ServidorDelegado delegado = new ServidorDelegado(socket, dataIn, dataOut);
                delegado.start();

            } catch (IOException e) {
                System.err.println("Error al manejar conexión: " + e.getMessage());
            }
        }
    }
}