package com.example.tfg_cooperativa.models;

import org.json.JSONObject;

public class Finca {

    private final int id, higueras, ciruelos, arandanos, cerezos;
    private final String localidad, direccion, imagen;


    public Finca(int id, String localidad, String direccion,
                 int higueras, int ciruelos, int arandanos, int cerezos, String imagen) {
        this.id = id;
        this.localidad = localidad;
        this.direccion = direccion;
        this.higueras = higueras;
        this.ciruelos = ciruelos;
        this.arandanos = arandanos;
        this.cerezos = cerezos;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public String getLocalidad() { return localidad; }
    public String getDireccion() { return direccion; }
    public int getHigueras() { return higueras; }
    public int getCiruelos() { return ciruelos; }
    public int getArandanos() { return arandanos; }
    public int getCerezos() { return cerezos; }
    public String getImagen() { return imagen; }

    public int getTotalArboles() {
        return higueras + ciruelos + arandanos + cerezos;
    }

    public static Finca fromJson(JSONObject o) {
        return new Finca(
                o.optInt("id"),
                o.optString("localidad", ""),
                o.optString("direccion", ""),
                o.optInt("higueras", 0),
                o.optInt("ciruelos", 0),
                o.optInt("arandanos", 0),
                o.optInt("cerezos", 0),
                o.isNull("imagen") ? null : o.optString("imagen", null)
        );
    }
}
