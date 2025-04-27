package Cliente;

import java.io.*;
import java.net.Socket;

public class Cliente {
    public static final int PUERTO = 8080;
    public static final String HOST = "localhost";

    public static void main(String args[]) throws IOException {
        System.out.println("Cliente ...");

        BufferedReader entradaConsola = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Seleccione el modo de operación:");
            System.out.println("1. Cliente iterativo");
            System.out.println("2. Cliente concurrente");
            System.out.println("0. Salir");
            System.out.print("Ingrese su opción: ");

            String opcion = entradaConsola.readLine();

            if (opcion.equals("0")) {
                System.out.println("Saliendo del cliente...");
                return;
            }

            if (opcion.equals("1")) {
                // Cliente iterativo normal
                Socket socket = new Socket(HOST, PUERTO);
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));

                ProtocoloCliente.procesarIterativo(entrada, dataIn, dataOut, socket);

                entrada.close();
                dataIn.close();
                dataOut.close();
                socket.close();

            } else if (opcion.equals("2")) {
                // Cliente concurrente
                System.out.print("¿Cuántos clientes concurrentes desea lanzar? Puede elejir entre los numeros 4,16,32,64: ");
                int cantidadClientes = Integer.parseInt(entradaConsola.readLine());

                for (int i = 0; i < cantidadClientes; i++) {
                    ClienteConcurrente cliente = new ClienteConcurrente(HOST, PUERTO);
                    cliente.start();
                }

            } else {
                System.out.println("Opción inválida. Terminando programa.");
                System.exit(1);
            }

            entradaConsola.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
