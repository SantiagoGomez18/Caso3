package Servidor;

public class Servicio {
    private int id;
    private String nombre;
    private String ip;
    private int puerto;

    public Servicio(int id, String nombre, String ip, int puerto) {
        this.id = id;
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

}