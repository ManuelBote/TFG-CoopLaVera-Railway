package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.session.GestorSesion;

import org.json.JSONObject;

public final class PerfilApi {

    private PerfilApi() {}

    public static void getPerfil(Context ctx, ApiCallback<JSONObject> cb) {
        ApiCliente.get(ctx, "perfil", new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject body, int code, String raw) {
                JSONObject user = body.optJSONObject("usuario");
                if (user != null) {
                    GestorSesion.get(ctx).updateProfile(
                            user.optString("nombre", ""),
                            user.optString("email", ""),
                            user.optString("telefono", ""),
                            user.optString("dni", "")
                    );
                }
                cb.onSuccess(user);
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    public static void actualizarPerfil(Context ctx, String nombre, String email,
                                        String telefono, String dni, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", nombre);
            body.put("email", email);
            body.put("telefono", telefono);
            body.put("dni", dni);
        } catch (Exception ignored) {}

        ApiCliente.put(ctx, "perfil", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) {
                GestorSesion.get(ctx).updateProfile(nombre, email, telefono, dni);
                cb.onSuccess(null);
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }
}
