package com.example.tfg_cooperativa.models;

import org.json.JSONObject;

public class UsuarioAdmin {

    public static final String PENDIENTE = "pendiente";
    public static final String ACEPTADO = "aceptado";
    public static final String RECHAZADO = "rechazado";

    private final int id;
    private final String nombre, apellidos, email, telefono, dni, direccion, localidad, estado;

    public UsuarioAdmin(int id, String nombre, String apellidos, String email,
                        String telefono, String dni, String direccion, String localidad,
                        String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
        this.direccion = direccion;
        this.localidad = localidad;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDni() { return dni; }
    public String getDireccion() { return direccion; }
    public String getLocalidad() { return localidad; }
    public String getEstado() { return estado; }

    public boolean esPendiente() { return PENDIENTE.equalsIgnoreCase(estado); }
    public boolean esAceptado() { return ACEPTADO.equalsIgnoreCase(estado); }
    public boolean esRechazado() {
        return RECHAZADO.equalsIgnoreCase(estado) || "rechazada".equalsIgnoreCase(estado);
    }

    public String getNombreCompleto() {
        if (apellidos == null || apellidos.isEmpty() || "null".equals(apellidos)) return nombre;
        return nombre + " " + apellidos;
    }

    private static String safe(JSONObject o, String key) {
        if (o.isNull(key)) return "";
        String v = o.optString(key, "");
        return "null".equals(v) ? "" : v;
    }

    public static UsuarioAdmin fromJson(JSONObject o) {
        return new UsuarioAdmin(
                o.optInt("id"),
                safe(o, "nombre"),
                safe(o, "apellidos"),
                safe(o, "email"),
                safe(o, "telefono"),
                safe(o, "dni"),
                safe(o, "direccion"),
                safe(o, "localidad"),
                safe(o, "estado")
        );
    }
}
