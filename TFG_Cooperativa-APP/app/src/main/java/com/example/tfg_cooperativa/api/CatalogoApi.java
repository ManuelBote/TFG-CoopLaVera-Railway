package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.data.RepositorioProductos;
import com.example.tfg_cooperativa.models.Producto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CatalogoApi {

    private CatalogoApi() {}

    public static void listMateriales(Context ctx, ApiCallback<List<Producto>> cb) {
        ApiCliente.get(ctx, "materiales", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                List<Producto> list = parseArray(body, Producto::fromMaterialJson);
                RepositorioProductos.get().setMaterials(list);
                cb.onSuccess(list);
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    public static void listProductos(Context ctx, ApiCallback<List<Producto>> cb) {
        ApiCliente.get(ctx, "productos", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                List<Producto> list = parseArray(body, Producto::fromProductoJson);
                RepositorioProductos.get().setProduce(list);
                cb.onSuccess(list);
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    // PUT /api/productos/{id} — solo actualiza los 4 precios
    public static void updateProducto(Context ctx, int id,
                                      double precioCongelado, double precioM,
                                      double precioL, double precioJumbo,
                                      ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("precio_congelado", precioCongelado);
            body.put("precio_m", precioM);
            body.put("precio_l", precioL);
            body.put("precio_jumbo", precioJumbo);
        } catch (Exception ignored) {}

        ApiCliente.put(ctx, "productos/" + id, body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    @FunctionalInterface
    private interface JsonToProducto {
        Producto parse(JSONObject obj);
    }

    private static List<Producto> parseArray(JSONObject body, JsonToProducto parser) {
        List<Producto> list = new ArrayList<>();
        JSONArray arr = body.optJSONArray("data");
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;
            list.add(parser.parse(obj));
        }
        Collections.reverse(list);
        return list;
    }
}
