package com.example.tfg_cooperativa.models;

import androidx.annotation.DrawableRes;


public class EntradaForo {

    private final int id;
    private final String titulo, fecha, emoji, contenido;
    @DrawableRes private final int imagenResId;

    public EntradaForo(int id, String titulo, String fecha, @DrawableRes int imagenResId,
                       String contenido) {
        this(id, titulo, fecha, "", contenido, imagenResId);
    }

    public EntradaForo(int id, String titulo, String fecha, String emoji, String contenido) {
        this(id, titulo, fecha, emoji, contenido, 0);
    }

    private EntradaForo(int id, String titulo, String fecha, String emoji, String contenido,
                        @DrawableRes int imagenResId) {
        this.id = id;
        this.titulo = titulo;
        this.fecha = fecha;
        this.emoji = emoji;
        this.contenido = contenido;
        this.imagenResId = imagenResId;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getFecha() { return fecha; }
    public String getEmoji() { return emoji; }
    public String getContenido() { return contenido; }
    @DrawableRes public int getImagenResId() { return imagenResId; }
    public boolean tieneImagen() { return imagenResId != 0; }
}
