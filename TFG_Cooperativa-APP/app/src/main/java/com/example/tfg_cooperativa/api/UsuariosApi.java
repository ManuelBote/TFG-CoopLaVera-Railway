package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.models.UsuarioAdmin;
import com.example.tfg_cooperativa.models.UsuarioResumen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UsuariosApi {

    private UsuariosApi() {}

    public static void listar(Context ctx, ApiCallback<List<UsuarioResumen>> cb) {
        ApiCliente.get(ctx, "usuarios", new ApiCliente.JsonCallback() {
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

    public static void listarDetallado(Context ctx, ApiCallback<List<UsuarioAdmin>> cb) {
        ApiCliente.get(ctx, "usuarios", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                cb.onSuccess(parseDetallado(body.optJSONArray("data")));
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    public static void cambiarEstado(Context ctx, int id, String estado, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("estado", estado);
        } catch (Exception ignored) {}

        ApiCliente.put(ctx, "usuarios/" + id, body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    public static void darDeBaja(Context ctx, int id, ApiCallback<Void> cb) {
        ApiCliente.put(ctx, "usuarios/" + id + "/baja", new JSONObject(),
                new ApiCliente.JsonCallback() {
                    @Override
                    public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
                    @Override
                    public void onError(String message, int code, String raw) { cb.onError(message); }
                });
    }

    private static List<UsuarioAdmin> parseDetallado(JSONArray arr) {
        List<UsuarioAdmin> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject u = arr.optJSONObject(i);
            if (u != null) out.add(UsuarioAdmin.fromJson(u));
        }
        Collections.reverse(out);
        out.sort((a, b) -> ordenEstado(a.getEstado()) - ordenEstado(b.getEstado()));
        return out;
    }

    private static int ordenEstado(String estado) {
        if (estado == null) return 3;
        switch (estado.toLowerCase()) {
            case "pendiente": return 0;
            case "aceptado": return 1;
            default: return 2; // rechazado u otros
        }
    }

    private static List<UsuarioResumen> parse(JSONArray arr) {
        List<UsuarioResumen> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject u = arr.optJSONObject(i);
            if (u == null) continue;
            int id = u.optInt("id");
            String nombre = u.optString("nombre", "Socio #" + id);
            String apellidos = u.optString("apellidos", "");
            if (apellidos != null && !apellidos.isEmpty() && !"null".equals(apellidos)) {
                nombre = nombre + " " + apellidos;
            }
            String email = u.optString("email", "");
            out.add(new UsuarioResumen(id, nombre, email));
        }
        return out;
    }
}
