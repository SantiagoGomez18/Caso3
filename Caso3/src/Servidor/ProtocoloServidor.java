package Servidor;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import Cifrados.Asimetrico;
import Cifrados.Firma;

public class ProtocoloServidor {

    public static void procesar(DataInputStream dataIn, DataOutputStream dataOut, Socket socket) throws IOException {
        try {
            // Variables para tiempos
            long tiempoTotalFirma = 0;
            long tiempoTotalCifrado = 0;
            long tiempoTotalVerificacion = 0;
            long tiempoTotalCifradoAsimetrico = 0;
            int cantidadConsultas = 0;

            // Leer mensaje inicial
            String mensaje = leerMensaje(dataIn);
            System.out.println("Mensaje recibido: " + mensaje);

            // Leer reto
            String reto = leerMensaje(dataIn);
            System.out.println("Reto recibido");

            // Cifrar y enviar respuesta
            PrivateKey llavePrivada = cargarLlavePriv();
            long inicioFirma = System.nanoTime();
            byte[] cifrado = Asimetrico.cifrar(llavePrivada, "RSA", reto);
            tiempoTotalFirma += (System.nanoTime() - inicioFirma);
            dataOut.writeInt(cifrado.length);
            dataOut.write(cifrado);
            dataOut.flush();

            // Esperar confirmación
            String respuesta = leerMensaje(dataIn);
            if (!"OK".equals(respuesta)) {
                System.out.println("Usuario mandó ERROR. Cerrando conexión.");
                return;
            }

            // Diffie-Hellman
            System.out.println("Generando parámetros Diffie-Hellman...");
            DHParameterSpec dhParameterSpec = generarParametrosDH();
            BigInteger p = dhParameterSpec.getP();
            BigInteger g = dhParameterSpec.getG();

            SecureRandom random = new SecureRandom();
            BigInteger x = new BigInteger(256, random);
            BigInteger gx = g.modPow(x, p);

            enviarBigInteger(dataOut, g);
            enviarBigInteger(dataOut, p);
            enviarBigInteger(dataOut, gx);

            byte[] firma = Firma.firmar(g, p, gx, llavePrivada);
            dataOut.writeInt(firma.length);
            dataOut.write(firma);
            dataOut.flush();

            String respuestaDH = leerMensaje(dataIn);
            if (!"OK".equals(respuestaDH)) {
                System.out.println("Usuario mandó ERROR después de DH. Cerrando conexión.");
                return;
            }

            BigInteger gy = recibirBigInteger(dataIn);

            BigInteger k = gy.modPow(x, p);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] hash = sha512.digest(k.toByteArray());
            byte[] K_AB1 = Arrays.copyOfRange(hash, 0, 32);
            byte[] K_AB2 = Arrays.copyOfRange(hash, 32, 64);

            SecretKeySpec AES = new SecretKeySpec(K_AB1, "AES");
            SecretKeySpec HMAC = new SecretKeySpec(K_AB2, "HmacSHA256");

            byte[] iv = new byte[16];
            dataIn.readFully(iv);

            // Enviar tabla de servicios
            TablaServicios tablaServicios = new TablaServicios();
            StringBuilder servicios = new StringBuilder();
            for (Servicio s : tablaServicios.getTodosLosServicios()) {
                servicios.append(s.getId()).append(": ").append(s.getNombre()).append("\n");
            }

            long inicioCifradoTabla = System.nanoTime();
            byte[] serviciosCifrados = Cifrados.Simetrico.cifrar(AES, iv, servicios.toString());
            tiempoTotalCifrado = (System.nanoTime() - inicioCifradoTabla);
            Long tiempofinaltabla = System.nanoTime();
            Long tf = tiempofinaltabla - inicioCifradoTabla;
            System.out.println("tiempo total cifrado tabla simetrico: " + tf);

            long asimetrico = System.nanoTime();
            byte[] a = Cifrados.Asimetrico.cifrar(llavePrivada, "RSA", servicios.toString());
            Long tiempoTotalCifradoa = (System.nanoTime() - asimetrico);
            System.out.println("Tiempo asimetrico tabla de servicio: " + tiempoTotalCifradoa);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(HMAC);
            byte[] hmacServicios = mac.doFinal(serviciosCifrados);

            dataOut.writeInt(serviciosCifrados.length);
            dataOut.write(serviciosCifrados);
            dataOut.writeInt(hmacServicios.length);
            dataOut.write(hmacServicios);
            dataOut.flush();
            System.out.println("Tabla de servicios enviada al cliente.");

            boolean continuar = true;

            while (continuar) {
                try {
                    // Leer mensaje cifrado
                    byte[] solicitudCifrada = new byte[dataIn.readInt()];
                    dataIn.readFully(solicitudCifrada);

                    // Leer HMAC
                    byte[] hmacSolicitud = new byte[dataIn.readInt()];
                    dataIn.readFully(hmacSolicitud);

                    // Verificar HMAC
                    mac.init(HMAC);
                    long inicioVerificacion = System.nanoTime();
                    byte[] hmacVerificado = mac.doFinal(solicitudCifrada);
                    tiempoTotalVerificacion += (System.nanoTime() - inicioVerificacion);

                    if (!Arrays.equals(hmacSolicitud, hmacVerificado)) {
                        System.out.println("Error en HMAC de solicitud. Cerrando conexión.");
                        break;
                    }

                    // Descifrar mensaje
                    String solicitudDescifrada = Cifrados.Simetrico.descifrar(AES, iv, solicitudCifrada);

                    if ("SALIR".equals(solicitudDescifrada)) {
                        System.out.println("Cliente iterativo envió 'SALIR'. Cerrando conexión.");
                        continuar = false;
                        continue;
                    }

                    // Procesar solicitud
                    String[] partes = solicitudDescifrada.split("-");
                    int idServicio = Integer.parseInt(partes[0]);
                    String ipCliente = partes[1];

                    Servicio servicio = tablaServicios.getServicioPorId(idServicio);
                    if (servicio == null) {
                        System.out.println("ID de servicio inválido. Cerrando conexión.");
                        continuar = false;
                        continue;
                    }

                    System.out
                            .println("Solicitud recibida: ID servicio = " + idServicio + ", IP cliente = " + ipCliente);

                    // Armar respuesta
                    String respuestaServicio = servicio.getIp() + "\n" + servicio.getPuerto();

                    long inicioCifradoRespuesta = System.nanoTime();
                    byte[] respuestaCifrada = Cifrados.Simetrico.cifrar(AES, iv, respuestaServicio);
                    tiempoTotalCifradoAsimetrico += (System.nanoTime() - inicioCifradoRespuesta);
                    System.out.println(
                            "Tiempo cifrado respuesta simetrica: " + (System.nanoTime() - inicioCifradoRespuesta));

                    long inicioCifradorespuestaasmin = System.nanoTime();
                    byte[] respuestaCifradaasmin = Cifrados.Simetrico.cifrar(AES, iv, respuestaServicio);
                    Long tiempofinal = System.nanoTime() - inicioCifradorespuestaasmin;
                    System.out.println("Tiempo cifrado respuesta asimetrica: " + tiempofinal);

                    mac.init(HMAC);
                    byte[] hmacRespuesta = mac.doFinal(respuestaCifrada);

                    dataOut.writeInt(respuestaCifrada.length);
                    dataOut.write(respuestaCifrada);
                    dataOut.writeInt(hmacRespuesta.length);
                    dataOut.write(hmacRespuesta);
                    dataOut.flush();
                    System.out.println("Respuesta enviada al cliente.");

                    cantidadConsultas++;

                } catch (IOException e) {
                    System.out.println("Cliente cerró conexión inesperadamente.");
                    continuar = false;
                } catch (Exception e) {
                    System.out.println("Error general: " + e.getMessage());
                    continuar = false;
                }
            }

            // Mostrar tiempos
            System.out.println("\n====== Resultado de Tiempos ======");
            System.out.println("Consultas realizadas: " + cantidadConsultas);
            System.out.println("Tiempo total de firma (ns): " + tiempoTotalFirma);
            System.out.println("Tiempo total de cifrado tabla (ns): " + tiempoTotalCifrado);
            System.out.println("Tiempo total de verificación (ns): " + tiempoTotalVerificacion);
            System.out.println("Tiempo total de cifrado respuesta (ns): " + tiempoTotalCifradoAsimetrico);
            System.out.println("==================================\n");

            // Registrar si es concurrente
            if (cantidadConsultas == 1) {
                RegistroTiemposConcurrente.registrarTiempos(
                        tiempoTotalFirma,
                        tiempoTotalCifrado,
                        tiempoTotalVerificacion,
                        tiempoTotalCifradoAsimetrico);
                RegistroTiemposConcurrente.mostrarPromedios();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enviarBigInteger(DataOutputStream out, BigInteger n) throws IOException {
        byte[] bytes = n.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    private static BigInteger recibirBigInteger(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new BigInteger(bytes);
    }

    private static String leerMensaje(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes);
    }

    private static PrivateKey cargarLlavePriv() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("Caso3/Caso3/src/llaves/llave_privada.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PrivateKey key = (PrivateKey) ois.readObject();
        ois.close();
        return key;
    }

    private static DHParameterSpec generarParametrosDH() throws Exception {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(1024);
        AlgorithmParameters params = paramGen.generateParameters();
        return params.getParameterSpec(DHParameterSpec.class);
    }
}
