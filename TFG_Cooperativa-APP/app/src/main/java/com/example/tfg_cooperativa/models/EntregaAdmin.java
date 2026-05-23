package com.example.tfg_cooperativa.models;

/**
 * Entrega de fruta vista desde el panel admin (gestión completa).
 * Mapea una fila de {@code GET /api/gestionEntregas} con todos sus campos,
 * para poder editarla o eliminarla ({@code PUT/DELETE /api/gestionEntregas/{id}}).
 */
public class EntregaAdmin {

    /** Estados de una entrega (columna {@code estado} del back). */
    public static final String PENDIENTE = "pendiente";
    public static final String ACEPTADO = "aceptado";
    public static final String RECHAZADO = "rechazado";

    private final int id, idUsuario, idProducto, cCongelados, cM, cL, cJumbo;
    private final String nombreProducto, fechaEntrega, estado;

    public EntregaAdmin(int id, int idUsuario, int idProducto, String nombreProducto,
                        String fechaEntrega, int cCongelados, int cM, int cL, int cJumbo,
                        String estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.fechaEntrega = fechaEntrega;
        this.cCongelados = cCongelados;
        this.cM = cM;
        this.cL = cL;
        this.cJumbo = cJumbo;
        this.estado = estado;
    }

    public int getId() { return id; }
    public int getIdUsuario() { return idUsuario; }
    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getFechaEntrega() { return fechaEntrega; }
    public int getCCongelados() { return cCongelados; }
    public int getCM() { return cM; }
    public int getCL() { return cL; }
    public int getCJumbo() { return cJumbo; }
    public String getEstado() { return estado; }

    public boolean esPendiente() { return PENDIENTE.equalsIgnoreCase(estado); }
    public boolean esAceptado() { return ACEPTADO.equalsIgnoreCase(estado); }
    public boolean esRechazado() { return RECHAZADO.equalsIgnoreCase(estado); }

    public int getTotal() { return cCongelados + cM + cL + cJumbo; }
}
