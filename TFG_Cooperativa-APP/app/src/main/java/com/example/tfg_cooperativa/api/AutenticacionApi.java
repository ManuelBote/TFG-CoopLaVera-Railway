package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.session.GestorSesion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public final class AutenticacionApi {

    private AutenticacionApi() {}

    public static void login(Context ctx, String email, String pass, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("pass", pass);
        } catch (JSONException ignored) {}

        ApiCliente.post(ctx, "login", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject obj, int code, String raw) {
                persistSession(ctx, obj, cb);
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }


    public static void register(Context ctx, String nombre, String apellidos,
                                 String dni, String email, String telefono,
                                 String direccion, String localidad,
                                 String password, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", nombre);
            body.put("apellidos", apellidos == null ? "" : apellidos);
            body.put("dni", dni);
            body.put("email", email);
            body.put("telefono", telefono == null ? "" : telefono);
            body.put("direccion", direccion == null ? "" : direccion);
            body.put("localidad", localidad == null ? "" : localidad);
            body.put("password", password);
            body.put("password2", password);
        } catch (JSONException ignored) {}

        ApiCliente.post(ctx, "registro", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject obj, int code, String raw) {
                cb.onSuccess(null); // cuenta creada, pendiente de aprobación
            }
            @Override
            public void onError(String message, int code, String raw) {
                cb.onError(message);
            }
        });
    }

    private static void persistSession(Context ctx, JSONObject obj, ApiCallback<Void> cb) {
        try {
            JSONObject user = obj.optJSONObject("usuario");
            if (user == null) {
                cb.onError("Respuesta inesperada del servidor");
                return;
            }

            // La cuenta solo puede entrar si está aceptada.
            String estado = user.optString("estado", "").toLowerCase(Locale.ROOT);
            if ("pendiente".equals(estado)) {
                cb.onError(ctx.getString(R.string.err_cuenta_pendiente));
                return;
            }
            if ("rechazado".equals(estado) || "rechazada".equals(estado)) {
                cb.onError(ctx.getString(R.string.err_cuenta_rechazada));
                return;
            }

            String token = obj.optString("token", "");
            int id = user.optInt("id", -1);
            String nombre = user.optString("nombre", "");
            String email = user.optString("correo", user.optString("email", ""));
            String telefono = user.optString("telefono", "");
            String dni = user.optString("dni", "");
            int tipo = user.optInt("tipo", 0);

            GestorSesion.get(ctx).login(
                    token, id, nombre, email, telefono, dni, tipo == 2);
            cb.onSuccess(null);
        } catch (Exception e) {
            cb.onError("No se pudo procesar la respuesta: " + e.getMessage());
        }
    }
}
