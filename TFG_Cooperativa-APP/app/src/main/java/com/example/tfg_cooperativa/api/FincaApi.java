package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.models.Finca;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FincaApi {

    private FincaApi() {}

    public static void misFincas(Context ctx, ApiCallback<List<Finca>> cb) {
        ApiCliente.get(ctx, "fincas", new ApiCliente.JsonCallback() {
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

    public static void crear(Context ctx, String localidad, String direccion,
                             int higueras, int ciruelos, int arandanos, int cerezos,
                             ApiCallback<Void> cb) {
        ApiCliente.post(ctx, "fincas",
                build(localidad, direccion, higueras, ciruelos, arandanos, cerezos),
                voidCallback(cb));
    }

    public static void actualizar(Context ctx, int id, String localidad, String direccion,
                                  int higueras, int ciruelos, int arandanos, int cerezos,
                                  ApiCallback<Void> cb) {
        ApiCliente.put(ctx, "fincas/" + id,
                build(localidad, direccion, higueras, ciruelos, arandanos, cerezos),
                voidCallback(cb));
    }

    public static void eliminar(Context ctx, int id, ApiCallback<Void> cb) {
        ApiCliente.delete(ctx, "fincas/" + id, voidCallback(cb));
    }


    private static JSONObject build(String localidad, String direccion,
                                    int higueras, int ciruelos, int arandanos, int cerezos) {
        JSONObject body = new JSONObject();
        try {
            body.put("localidad", localidad);
            body.put("direccion", direccion);
            body.put("higueras", higueras);
            body.put("ciruelos", ciruelos);
            body.put("arandanos", arandanos);
            body.put("cerezos", cerezos);
        } catch (Exception ignored) {}
        return body;
    }

    private static ApiCliente.JsonCallback voidCallback(ApiCallback<Void> cb) {
        return new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        };
    }

    private static List<Finca> parse(JSONArray arr) {
        List<Finca> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) out.add(Finca.fromJson(o));
        }
        Collections.reverse(out);
        return out;
    }
}
