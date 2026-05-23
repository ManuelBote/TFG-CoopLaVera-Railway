package com.example.tfg_cooperativa.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SolicitudAlquiler {

    public static final String PENDIENTE = "pendiente";
    public static final String ACEPTADA = "aceptada";
    public static final String RECHAZADA = "rechazada";

    private final int id, idUsuario;
    private final String nombreUsuario, estado;
    private final Date fecha;
    private final List<Linea> detalles;

    public SolicitudAlquiler(int id, int idUsuario, String nombreUsuario,
                             String estado, Date fecha, List<Linea> detalles) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.estado = estado;
        this.fecha = fecha;
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
    }

    public int getId() { return id; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getEstado() { return estado; }
    public Date getFecha() { return fecha; }
    public List<Linea> getDetalles() { return detalles; }

    public boolean esPendiente() { return PENDIENTE.equalsIgnoreCase(estado); }
    public boolean esAceptada() { return ACEPTADA.equalsIgnoreCase(estado); }
    public boolean esRechazada() { return RECHAZADA.equalsIgnoreCase(estado); }

    public static class Linea {
        private final String nombreMaterial;
        private final int cantidad;

        public Linea(String nombreMaterial, int cantidad) {
            this.nombreMaterial = nombreMaterial;
            this.cantidad = cantidad;
        }

        public String getNombreMaterial() { return nombreMaterial; }
        public int getCantidad() { return cantidad; }
    }
}
