package Cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import Cifrados.Asimetrico;

public class ProtocoloCliente {

    public static void procesarIterativo(BufferedReader stdIn, DataInputStream dataIn, DataOutputStream dataOut, Socket socket) throws IOException {
        try {
            // Saludo inicial
            enviarMensaje(dataOut, "HELLO");
    
            String reto = String.valueOf(new SecureRandom().nextInt(1000000));
            enviarMensaje(dataOut, reto);
    
            byte[] cifrado = new byte[dataIn.readInt()];
            dataIn.readFully(cifrado);
    
            PublicKey llavePublica = cargarLlavePub();
            String descifrado = new String(Asimetrico.descifrar(llavePublica, "RSA", cifrado));
            System.out.println("Descifrado del reto: " + descifrado);
    
            if (reto.equals(descifrado)) {
                enviarMensaje(dataOut, "OK");
                System.out.println("Cliente mandó OK tras verificar el reto.");
            } else {
                enviarMensaje(dataOut, "ERROR");
                System.out.println("Cliente mandó ERROR tras fallar la verificación del reto.");
                return;
            }
    
            // Diffie-Hellman
            BigInteger g = recibirBigInteger(dataIn);
            BigInteger p = recibirBigInteger(dataIn);
            BigInteger gx = recibirBigInteger(dataIn);
    
            byte[] firma = new byte[dataIn.readInt()];
            dataIn.readFully(firma);
    
            boolean firmaValida = Cifrados.Firma.verificar(g, p, gx, firma, llavePublica);
            if (firmaValida) {
                enviarMensaje(dataOut, "OK");
                System.out.println("Cliente mandó OK tras verificar la firma DH.");
            } else {
                enviarMensaje(dataOut, "ERROR");
                System.out.println("Cliente mandó ERROR tras fallar la verificación de la firma DH.");
                return;
            }
    
            // Enviar Gy
            SecureRandom random = new SecureRandom();
            BigInteger y = new BigInteger(256, random);
            BigInteger gy = g.modPow(y, p);
    
            enviarBigInteger(dataOut, gy);
            System.out.println("Cliente envió Gy.");
    
            // Enviar IV
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            dataOut.write(iv);
            dataOut.flush();
            System.out.println("Cliente envió IV.");
    
            // Calcular llave compartida
            BigInteger k = gx.modPow(y, p);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] hash = sha512.digest(k.toByteArray());
            byte[] K_AB1 = Arrays.copyOfRange(hash, 0, 32);
            byte[] K_AB2 = Arrays.copyOfRange(hash, 32, 64);
    
            SecretKeySpec AES = new SecretKeySpec(K_AB1, "AES");
            SecretKeySpec HMAC = new SecretKeySpec(K_AB2, "HmacSHA256");
    
            // Recibir servicios
            byte[] serviciosCifrados = new byte[dataIn.readInt()];
            dataIn.readFully(serviciosCifrados);
    
            byte[] hmacServicios = new byte[dataIn.readInt()];
            dataIn.readFully(hmacServicios);
    
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(HMAC);
            byte[] hmacCalculado = mac.doFinal(serviciosCifrados);
    
            if (!Arrays.equals(hmacServicios, hmacCalculado)) {
                System.out.println("HMAC de servicios incorrecto. Terminando.");
                return;
            }
            System.out.println("Servicios disponibles:");
            System.out.println(Cifrados.Simetrico.descifrar(AES, iv, serviciosCifrados));
    
            // Solicitudes múltiples
            Random rnd = new Random();
            int contadorSolicitudes = 0;
            boolean continuar = true;
    
            while (continuar) {
                contadorSolicitudes++;
    
                int idServicio = rnd.nextInt(3) + 1;
                String ipCliente = "Localhost:8080";
    
                String solicitud = idServicio + "-" + ipCliente;
                byte[] solicitudCifrada = Cifrados.Simetrico.cifrar(AES, iv, solicitud);
    
                mac.init(HMAC);
                byte[] hmacSolicitud = mac.doFinal(solicitudCifrada);
    
                dataOut.writeInt(solicitudCifrada.length);
                dataOut.write(solicitudCifrada);
                dataOut.writeInt(hmacSolicitud.length);
                dataOut.write(hmacSolicitud);
                dataOut.flush();
    
                System.out.println("Solicitud #" + contadorSolicitudes + " enviada (ID servicio: " + idServicio + ")");
    
                byte[] respuestaCifrada = new byte[dataIn.readInt()];
                dataIn.readFully(respuestaCifrada);
                byte[] hmacRespuesta = new byte[dataIn.readInt()];
                dataIn.readFully(hmacRespuesta);
    
                mac.init(HMAC);
                byte[] hmacRespuestaCalculado = mac.doFinal(respuestaCifrada);
    
                if (!Arrays.equals(hmacRespuesta, hmacRespuestaCalculado)) {
                    System.out.println("HMAC de respuesta incorrecto. Terminando.");
                    return;
                }
    
                String respuesta = Cifrados.Simetrico.descifrar(AES, iv, respuestaCifrada);
                System.out.println("Respuesta recibida:\n" + respuesta);
    
                if (contadorSolicitudes >= 32) {
                    continuar = false;
                }
            }
            // Enviar "SALIR" cuando termine
            byte[] salirCifrado = Cifrados.Simetrico.cifrar(AES, iv, "SALIR");
            mac.init(HMAC);
            byte[] hmacSalir = mac.doFinal(salirCifrado);
    
            dataOut.writeInt(salirCifrado.length);
            dataOut.write(salirCifrado);
            dataOut.writeInt(hmacSalir.length);
            dataOut.write(hmacSalir);
            dataOut.flush();
    
            System.out.println("Cliente envió mensaje de salida después de 32 solicitudes.");
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static void procesarConcurrente(BufferedReader stdIn, DataInputStream dataIn, DataOutputStream dataOut, Socket socket) throws IOException {
        try {
            System.out.println("Nueva conexión aceptada. Procesando cliente concurrente...");
            // Mensaje inicial
            enviarMensaje(dataOut, "HELLO");
    
            String reto = String.valueOf(new SecureRandom().nextInt(1000000));
            enviarMensaje(dataOut, reto);
    
            byte[] cifrado = new byte[dataIn.readInt()];
            dataIn.readFully(cifrado);
    
            PublicKey llavePublica = cargarLlavePub();
            String descifrado = new String(Asimetrico.descifrar(llavePublica, "RSA", cifrado));
            System.out.println("Descifrado del reto: " + descifrado);
    
            if (reto.equals(descifrado)) {
                enviarMensaje(dataOut, "OK");
                System.out.println("Cliente mandó OK tras verificar el reto.");
            } else {
                enviarMensaje(dataOut, "ERROR");
                System.out.println("Cliente mandó ERROR tras fallar la verificación del reto.");
                return;
            }
    
            // Diffie-Hellman
            BigInteger g = recibirBigInteger(dataIn);
            BigInteger p = recibirBigInteger(dataIn);
            BigInteger gx = recibirBigInteger(dataIn);
    
            byte[] firma = new byte[dataIn.readInt()];
            dataIn.readFully(firma);
    
            boolean firmaValida = Cifrados.Firma.verificar(g, p, gx, firma, llavePublica);
            if (firmaValida) {
                enviarMensaje(dataOut, "OK");
                System.out.println("Cliente mandó OK tras verificar la firma DH.");
            } else {
                enviarMensaje(dataOut, "ERROR");
                System.out.println("Cliente mandó ERROR tras fallar la verificación de la firma DH.");
                return;
            }
    
            // Generar y enviar Gy
            SecureRandom random = new SecureRandom();
            BigInteger y = new BigInteger(256, random);
            BigInteger gy = g.modPow(y, p);
    
            enviarBigInteger(dataOut, gy);
            System.out.println("Cliente envió Gy.");
    
            // Enviar IV
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            dataOut.write(iv);
            dataOut.flush();
            System.out.println("Cliente envió IV.");
    
            // Calcular llave secreta compartida
            BigInteger k = gx.modPow(y, p);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] hash = sha512.digest(k.toByteArray());
            byte[] K_AB1 = Arrays.copyOfRange(hash, 0, 32);
            byte[] K_AB2 = Arrays.copyOfRange(hash, 32, 64);
    
            SecretKeySpec AES = new SecretKeySpec(K_AB1, "AES");
            SecretKeySpec HMAC = new SecretKeySpec(K_AB2, "HmacSHA256");
    
            // Recibir servicios cifrados
            byte[] serviciosCifrados = new byte[dataIn.readInt()];
            dataIn.readFully(serviciosCifrados);
    
            byte[] hmacServicios = new byte[dataIn.readInt()];
            dataIn.readFully(hmacServicios);
    
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(HMAC);
            byte[] hmacCalculado = mac.doFinal(serviciosCifrados);
    
            if (!Arrays.equals(hmacServicios, hmacCalculado)) {
                System.out.println("HMAC de servicios incorrecto. Terminando.");
                return;
            }
            System.out.println("Servicios disponibles:");
            System.out.println(Cifrados.Simetrico.descifrar(AES, iv, serviciosCifrados));
    
            //1 sola solicitud
            Random rnd = new Random();
            int idServicio = rnd.nextInt(3) + 1;
            String ipCliente = "Localhost:8080";
    
            String solicitud = idServicio + "-" + ipCliente;
            byte[] solicitudCifrada = Cifrados.Simetrico.cifrar(AES, iv, solicitud);
    
            mac.init(HMAC);
            byte[] hmacSolicitud = mac.doFinal(solicitudCifrada);
    
            dataOut.writeInt(solicitudCifrada.length);
            dataOut.write(solicitudCifrada);
            dataOut.writeInt(hmacSolicitud.length);
            dataOut.write(hmacSolicitud);
            dataOut.flush();
    
            System.out.println("Solicitud enviada (ID servicio: " + idServicio + ")");
    
            // Recibir respuesta
            byte[] respuestaCifrada = new byte[dataIn.readInt()];
            dataIn.readFully(respuestaCifrada);
            byte[] hmacRespuesta = new byte[dataIn.readInt()];
            dataIn.readFully(hmacRespuesta);
    
            mac.init(HMAC);
            byte[] hmacRespuestaCalculado = mac.doFinal(respuestaCifrada);
    
            if (!Arrays.equals(hmacRespuesta, hmacRespuestaCalculado)) {
                System.out.println("HMAC de respuesta incorrecto. Terminando.");
                return;
            }
    
            String respuesta = Cifrados.Simetrico.descifrar(AES, iv, respuestaCifrada);
            System.out.println("Respuesta recibida:\n" + respuesta);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static void enviarMensaje(DataOutputStream out, String mensaje) throws IOException {
        byte[] bytes = mensaje.getBytes();
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

    private static void enviarBigInteger(DataOutputStream out, BigInteger n) throws IOException {
        byte[] bytes = n.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    private static PublicKey cargarLlavePub() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("Caso3/src/Llaves/llave_publica.key");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PublicKey key = (PublicKey) ois.readObject();
        ois.close();
        return key;
    }
}
