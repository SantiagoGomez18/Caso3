package Servidor;

import java.util.*;

public class TablaServicios {
    private Map<Integer, Servicio> servicios;

    public TablaServicios() {
        servicios = new HashMap<>();
        servicios.put(1, new Servicio(1, "Consultar estado del vuelo", "10.128.0.6", 8000));
        servicios.put(2, new Servicio(2, "Ver disponibilidad", "10.128.0.5", 8001));
        servicios.put(3, new Servicio(3, "Consultar precio", "10.128.0.0", 8002));
    }

    public Servicio getServicioPorId(int id) {
        return servicios.get(id);
    }

    public Collection<Servicio> getTodosLosServicios() {
        return servicios.values();
    }

    public boolean contieneId(int id) {
        return servicios.containsKey(id);
    }
}