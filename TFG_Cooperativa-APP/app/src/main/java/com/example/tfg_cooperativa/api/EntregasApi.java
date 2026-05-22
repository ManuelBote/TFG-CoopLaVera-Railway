package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.data.RepositorioProductos;
import com.example.tfg_cooperativa.models.EntregaAdmin;
import com.example.tfg_cooperativa.models.Producto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntregasApi {

    private EntregasApi() {}

    public static void crearEntrega(Context ctx, int idUsuario, int idProducto,
                                     String fechaIso,
                                     int cCongelados, int cM, int cL, int cJumbo,
                                     ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("id_usuario", idUsuario);
            body.put("id_producto", idProducto);
            body.put("fecha_entrega", fechaIso);
            body.put("cCongelados", cCongelados);
            body.put("cM", cM);
            body.put("cL", cL);
            body.put("cJumbo", cJumbo);
        } catch (Exception ignored) {}

        ApiCliente.post(ctx, "gestionEntregas", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    // Entregas del usuario actual
    public static void entregasUsuario(Context ctx, ApiCallback<JSONObject> cb) {
        ApiCliente.get(ctx, "entregas", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                cb.onSuccess(body.optJSONObject("entregas"));
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    // Listado admin de entregas
    public static void listAdmin(Context ctx, ApiCallback<JSONObject> cb) {
        ApiCliente.get(ctx, "gestionEntregas", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) { cb.onSuccess(body); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }


    public static void listAdminDetallado(Context ctx, ApiCallback<List<EntregaAdmin>> cb) {
        ApiCliente.get(ctx, "gestionEntregas", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                cb.onSuccess(parse(body.optJSONArray("data")));
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    // PUT /api/gestionEntregas/{id} — actualiza una entrega (solo admin).
    public static void actualizarEntrega(Context ctx, int id, int idUsuario, int idProducto,
                                         String fechaIso, int cCongelados, int cM, int cL, int cJumbo,
                                         ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("id_usuario", idUsuario);
            body.put("id_producto", idProducto);
            body.put("fecha_entrega", fechaIso);
            body.put("cCongelados", cCongelados);
            body.put("cM", cM);
            body.put("cL", cL);
            body.put("cJumbo", cJumbo);
        } catch (Exception ignored) {}

        ApiCliente.put(ctx, "gestionEntregas/" + id, body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    // DELETE /api/gestionEntregas/{id} — elimina una entrega (solo admin).
    public static void eliminarEntrega(Context ctx, int id, ApiCallback<Void> cb) {
        ApiCliente.delete(ctx, "gestionEntregas/" + id, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    private static List<EntregaAdmin> parse(JSONArray arr) {
        List<EntregaAdmin> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.optJSONObject(i);
            if (e == null) continue;
            int idProducto = e.optInt("id_producto");
            Producto p = RepositorioProductos.get().findProduce(idProducto);
            String nombre = p != null ? p.getName() : ("Producto #" + idProducto);
            out.add(new EntregaAdmin(
                    e.optInt("id"),
                    e.optInt("id_usuario"),
                    idProducto,
                    nombre,
                    e.optString("fecha_entrega", ""),
                    e.optInt("cantidad_congelados"),
                    e.optInt("cantidad_m"),
                    e.optInt("cantidad_l"),
                    e.optInt("cantidad_jumbo")
            ));
        }
        Collections.reverse(out);
        return out;
    }
}
