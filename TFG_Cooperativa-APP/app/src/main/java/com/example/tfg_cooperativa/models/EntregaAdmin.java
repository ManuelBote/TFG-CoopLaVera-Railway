package com.example.tfg_cooperativa.models;

/**
 * Entrega de fruta vista desde el panel admin (gestión completa).
 * Mapea una fila de {@code GET /api/gestionEntregas} con todos sus campos,
 * para poder editarla o eliminarla ({@code PUT/DELETE /api/gestionEntregas/{id}}).
 */
public class EntregaAdmin {

    private final int id, idUsuario, idProducto, cCongelados, cM, cL, cJumbo;
    private final String nombreProducto, fechaEntrega;

    public EntregaAdmin(int id, int idUsuario, int idProducto, String nombreProducto,
                        String fechaEntrega, int cCongelados, int cM, int cL, int cJumbo) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.fechaEntrega = fechaEntrega;
        this.cCongelados = cCongelados;
        this.cM = cM;
        this.cL = cL;
        this.cJumbo = cJumbo;
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

    public int getTotal() { return cCongelados + cM + cL + cJumbo; }
}
