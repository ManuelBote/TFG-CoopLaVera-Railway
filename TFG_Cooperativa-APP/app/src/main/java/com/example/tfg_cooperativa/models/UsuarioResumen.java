package com.example.tfg_cooperativa.models;

import androidx.annotation.NonNull;

public class UsuarioResumen {

    private final int id;
    private final String nombre, email;

    public UsuarioResumen(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }

    @NonNull
    @Override
    public String toString() {
        if (email == null || email.isEmpty()) return nombre;
        return nombre + " (" + email + ")";
    }
}
