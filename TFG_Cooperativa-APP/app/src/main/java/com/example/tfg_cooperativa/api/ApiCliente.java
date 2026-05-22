package com.example.tfg_cooperativa.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.tfg_cooperativa.session.GestorSesion;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public final class ApiCliente {

    public static final String BASE_URL =
            "https://tfg-cooplavera-back-production.up.railway.app/api/";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(new HttpLoggingInterceptor(
                    msg -> android.util.Log.d("ApiCliente", msg))
                    .setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

    private ApiCliente() {}


    public static void get(@NonNull Context ctx, @NonNull String path, @NonNull JsonCallback cb) {
        Request.Builder rb = new Request.Builder().url(BASE_URL + path).get();
        attachAuth(ctx, rb);
        enqueue(rb.build(), cb);
    }

    public static void post(@NonNull Context ctx, @NonNull String path,
                            @NonNull JSONObject body, @NonNull JsonCallback cb) {
        Request.Builder rb = new Request.Builder()
                .url(BASE_URL + path)
                .post(RequestBody.create(body.toString(), JSON));
        attachAuth(ctx, rb);
        enqueue(rb.build(), cb);
    }

    public static void put(@NonNull Context ctx, @NonNull String path,
                           @NonNull JSONObject body, @NonNull JsonCallback cb) {
        Request.Builder rb = new Request.Builder()
                .url(BASE_URL + path)
                .put(RequestBody.create(body.toString(), JSON));
        attachAuth(ctx, rb);
        enqueue(rb.build(), cb);
    }

    public static void delete(@NonNull Context ctx, @NonNull String path, @NonNull JsonCallback cb) {
        Request.Builder rb = new Request.Builder().url(BASE_URL + path).delete();
        attachAuth(ctx, rb);
        enqueue(rb.build(), cb);
    }


    private static void attachAuth(Context ctx, Request.Builder rb) {
        String token = GestorSesion.get(ctx).getToken();
        if (token != null && !token.isEmpty()) {
            rb.header("Authorization", "Bearer " + token);
        }
        rb.header("Accept", "application/json");
    }

    private static void enqueue(Request request, JsonCallback cb) {
        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                final String msg = "Error de red: " + e.getMessage();
                postToMain(() -> cb.onError(msg, -1, null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                final int code = response.code();
                String body = "";
                try (ResponseBody rb = response.body()) {
                    if (rb != null) body = rb.string();
                } catch (IOException ioe) {
                    postToMain(() -> cb.onError("Error leyendo respuesta", code, null));
                    return;
                }
                final String raw = body;

                if (!response.isSuccessful()) {
                    final String msg = extractMessage(raw);
                    postToMain(() -> cb.onError(msg, code, raw));
                    return;
                }

                JSONObject parsed;
                try {
                    parsed = raw.isEmpty() ? new JSONObject() : new JSONObject(raw);
                } catch (Exception ex) {
                    // La respuesta puede ser un array; envolverlo para uniformar.
                    try {
                        parsed = new JSONObject().put("data", new org.json.JSONArray(raw));
                    } catch (Exception ex2) {
                        postToMain(() -> cb.onError("Respuesta no es JSON válido", code, raw));
                        return;
                    }
                }
                final JSONObject json = parsed;
                postToMain(() -> cb.onSuccess(json, code, raw));
            }
        });
    }


    private static String extractMessage(String raw) {
        try {
            JSONObject obj = new JSONObject(raw);
            if (obj.has("mensaje")) return obj.getString("mensaje");
        } catch (Exception ignored) {}
        return raw.isEmpty() ? "Error desconocido" : raw;
    }

    private static void postToMain(Runnable r) {
        MAIN.post(r);
    }


    public interface JsonCallback {
        void onSuccess(JSONObject body, int code, String raw);
        void onError(String message, int code, String raw);
    }
}
