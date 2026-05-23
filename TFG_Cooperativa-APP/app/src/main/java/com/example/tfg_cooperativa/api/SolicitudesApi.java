package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.models.SolicitudAlquiler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public final class SolicitudesApi {

    private static final SimpleDateFormat ISO_DATETIME =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat ISO_DATE =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private SolicitudesApi() {}

    public static void listar(Context ctx, ApiCallback<List<SolicitudAlquiler>> cb) {
        ApiCliente.get(ctx, "solicitudes-material", new ApiCliente.JsonCallback() {
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

    public static void crear(Context ctx, int idUsuario, String fechaIso,
                             List<int[]> detalles, ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("id_usuario", idUsuario);
            body.put("fecha", fechaIso);
            JSONArray arr = new JSONArray();
            for (int[] d : detalles) {
                JSONObject o = new JSONObject();
                o.put("id_material", d[0]);
                o.put("cantidad", d[1]);
                arr.put(o);
            }
            body.put("detalles", arr);
        } catch (Exception ignored) {}

        ApiCliente.post(ctx, "solicitudes-material", body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }

    public static void actualizarEstado(Context ctx, int id, String estado,
                                        ApiCallback<Void> cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("estado", estado);
        } catch (Exception ignored) {}

        ApiCliente.put(ctx, "solicitudes-material/" + id, body, new ApiCliente.JsonCallback() {
            @Override
            public void onSuccess(JSONObject jb, int code, String raw) { cb.onSuccess(null); }
            @Override
            public void onError(String message, int code, String raw) { cb.onError(message); }
        });
    }


    private static List<SolicitudAlquiler> parse(JSONArray arr) {
        List<SolicitudAlquiler> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject s = arr.optJSONObject(i);
            if (s == null) continue;

            int id = s.optInt("id");
            int idUsuario = s.optInt("id_usuario");
            String estado = s.optString("estado", SolicitudAlquiler.PENDIENTE);

            // Nombre del socio
            String nombreUsuario;
            JSONObject usuario = s.optJSONObject("usuario");
            if (usuario != null) {
                nombreUsuario = usuario.optString("nombre", "Socio #" + idUsuario);
            } else {
                nombreUsuario = "Socio #" + idUsuario;
            }

            // Fecha
            Date fecha = parseDate(s.optString("fecha_solicitud", ""));
            if (fecha == null) fecha = parseDate(s.optString("created_at", ""));

            // Detalles
            List<SolicitudAlquiler.Linea> lineas = new ArrayList<>();
            JSONArray detalles = s.optJSONArray("detalles");
            if (detalles != null) {
                for (int j = 0; j < detalles.length(); j++) {
                    JSONObject d = detalles.optJSONObject(j);
                    if (d == null) continue;
                    int cantidad = d.optInt("cantidad", 0);
                    String nombreMaterial;
                    JSONObject material = d.optJSONObject("material");
                    if (material != null) {
                        nombreMaterial = material.optString("nombre",
                                "Material #" + d.optInt("id_material"));
                    } else {
                        nombreMaterial = "Material #" + d.optInt("id_material");
                    }
                    lineas.add(new SolicitudAlquiler.Linea(nombreMaterial, cantidad));
                }
            }

            out.add(new SolicitudAlquiler(id, idUsuario, nombreUsuario, estado, fecha, lineas));
        }
        Collections.reverse(out);
        return out;
    }

    private static Date parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            if (raw.length() >= 19) return ISO_DATETIME.parse(raw.substring(0, 19));
            if (raw.length() >= 10) return ISO_DATE.parse(raw.substring(0, 10));
        } catch (ParseException ignored) {}
        return null;
    }
}
