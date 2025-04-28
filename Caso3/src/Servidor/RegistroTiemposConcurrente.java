package Servidor;

public class RegistroTiemposConcurrente {

    private static long totalFirma = 0;
    private static long totalCifradoTabla = 0;
    private static long totalVerificacion = 0;
    private static long totalCifradoRespuesta = 0;
    private static int totalClientes = 0;

    public static synchronized void registrarTiempos(long tiempoFirma, long tiempoCifrado, long tiempoVerificacion,
            long tiempoCifradoRespuesta) {
        totalFirma += tiempoFirma;
        totalCifradoTabla += tiempoCifrado;
        totalVerificacion += tiempoVerificacion;
        totalCifradoRespuesta += tiempoCifradoRespuesta;
        totalClientes++;
    }

    public static void mostrarPromedios() {
        if (totalClientes == 0) {
            System.out.println("\n====== No se registraron tiempos concurrentes ======\n");
            return;
        }

        long promedioFirma = totalFirma / totalClientes;
        long promedioCifradoTabla = totalCifradoTabla / totalClientes;
        long promedioVerificacion = totalVerificacion / totalClientes;
        long promedioCifradoRespuesta = totalCifradoRespuesta / totalClientes;

        System.out.println("\n====== Promedios de Tiempos (Concurrentes) ======");
        System.out.println("Clientes Concurrentes: " + totalClientes);
        System.out.println("Promedio Firma (ns): " + promedioFirma);
        System.out.println("Promedio Cifrado Tabla (ns): " + promedioCifradoTabla);
        System.out.println("Promedio Verificación (ns): " + promedioVerificacion);
        System.out.println("Promedio Cifrado Respuesta (ns): " + promedioCifradoRespuesta);
        System.out.println("==================================\n");
    }

    public static void resetear() {
        totalFirma = 0;
        totalCifradoTabla = 0;
        totalVerificacion = 0;
        totalCifradoRespuesta = 0;
        totalClientes = 0;
    }
}
