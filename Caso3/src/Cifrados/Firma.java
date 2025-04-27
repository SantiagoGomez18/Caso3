package Cifrados;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class Firma {


    public static byte[] firmar(BigInteger g, BigInteger p, BigInteger gx, PrivateKey llavePrivada) throws Exception {

        ByteArrayOutputStream datos = new ByteArrayOutputStream();
        DataOutputStream datosOut = new DataOutputStream(datos);
        datosOut.write(g.toByteArray());
        datosOut.write(p.toByteArray());
        datosOut.write(gx.toByteArray());
        datosOut.flush();
        byte[] datosParaFirmar = datos.toByteArray();

        Signature firma = Signature.getInstance("SHA256withRSA");
        
        firma.initSign(llavePrivada);

        firma.update(datosParaFirmar);
        return firma.sign();
    }

    public static boolean verificar(BigInteger g, BigInteger p, BigInteger gx, byte[] firmaBytes, PublicKey llavePublica) throws Exception {

        ByteArrayOutputStream datos = new ByteArrayOutputStream();
        DataOutputStream datosOut = new DataOutputStream(datos);
        datosOut.write(g.toByteArray());
        datosOut.write(p.toByteArray());
        datosOut.write(gx.toByteArray());
        datosOut.flush();
        byte[] datosParaVerificar = datos.toByteArray();


        Signature firma = Signature.getInstance("SHA256withRSA");

        firma.initVerify(llavePublica);

        firma.update(datosParaVerificar);
        return firma.verify(firmaBytes);
    }
}
