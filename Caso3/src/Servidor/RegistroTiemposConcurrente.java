package Servidor;

public class RegistroTiemposConcurrente {

    private static long totalFirma = 0;
    private static long totalCifrado = 0;
    private static long totalVerificacion = 0;
    private static long totalCifradoAsimetrico = 0;
    private static int cantidadClientes = 0;

    public static synchronized void registrarTiempos(long firma, long cifrado, long verificacion, long cifradoAsim) {
        totalFirma += firma;
        totalCifrado += cifrado;
        totalVerificacion += verificacion;
        totalCifradoAsimetrico += cifradoAsim;
        cantidadClientes++;
    }

    public static synchronized void imprimirResultados() {
        if (cantidadClientes == 0) {
            System.out.println("No se registraron tiempos concurrentes.");
            return;
        }

        System.out.println("\n====== Resultado de Tiempos Concurrentes ======");
        System.out.println("Clientes concurrentes: " + cantidadClientes);
        System.out.println("Tiempo total de firma (ms): " + totalFirma);
        System.out.println("Tiempo total de cifrado tabla (ms): " + totalCifrado);
        System.out.println("Tiempo total de verificación (ms): " + totalVerificacion);
        System.out.println("Tiempo total de cifrado respuesta (ms): " + totalCifradoAsimetrico);
        System.out.println("==================================\n");

        reset();
    }

    private static void reset() {
        totalFirma = 0;
        totalCifrado = 0;
        totalVerificacion = 0;
        totalCifradoAsimetrico = 0;
        cantidadClientes = 0;
    }
}
