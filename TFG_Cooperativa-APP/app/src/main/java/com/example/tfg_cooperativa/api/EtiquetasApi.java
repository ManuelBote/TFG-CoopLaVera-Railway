package com.example.tfg_cooperativa.api;

import android.content.Context;

import org.json.JSONObject;

public final class EtiquetasApi {

    private EtiquetasApi() {}

    public static void crear(Context ctx, int idUsuario, int idFinca,
                             int higoFresco, int higoSeco, int arandano,
                             int cereza, int ciruela, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("idUsuario", idUsuario);
            body.put("idFinca", idFinca);
            body.put("higo_fresco", higoFresco);
            body.put("higo_seco", higoSeco);
            body.put("arandano", arandano);
            body.put("cereza", cereza);
            body.put("ciruela", ciruela);
        } catch (Exception ignored) {}

        ApiCliente.post(ctx, "etiquetas", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }
}
