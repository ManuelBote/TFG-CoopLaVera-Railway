package com.example.tfg_cooperativa.api;

import android.content.Context;

import com.example.tfg_cooperativa.R;

import java.util.Locale;
public final class MapeadorErrores {

    public enum Campo { EMAIL, PASSWORD, DNI, GENERAL }

    public static final class Resultado {
        public final Campo campo;
        public final String mensaje;
        public Resultado(Campo campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }
    }

    private MapeadorErrores() {}

    public static Resultado paraLogin(Context ctx, String raw) {
        String low = lower(raw);
        // Error de red detectado en ApiCliente
        if (low.contains("error de red") || low.contains("network")
                || low.contains("timeout") || low.contains("unable to resolve")) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_network));
        }
        // Credenciales mal
        if (low.contains("login incorrecto")
                || low.contains("credenciales")
                || low.contains("unauthorized")
                || low.contains("invalid credentials")) {
            return new Resultado(Campo.PASSWORD,
                    ctx.getString(R.string.err_credentials_invalid));
        }
        // Validación de email
        if (low.contains("email") && (low.contains("required") || low.contains("must") || low.contains("invalid"))) {
            return new Resultado(Campo.EMAIL, ctx.getString(R.string.err_email_invalid));
        }
        if (low.contains("pass") && low.contains("required")) {
            return new Resultado(Campo.PASSWORD, ctx.getString(R.string.err_password_required));
        }
        // SQL / 500
        if (low.contains("sqlstate") || low.contains("server error")
                || low.contains("internal server")) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_server));
        }
        // Caso genérico
        if (raw == null || raw.isEmpty() || looksLikeStackTrace(raw)) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_unknown));
        }
        return new Resultado(Campo.GENERAL, raw);
    }

    public static Resultado paraRegistro(Context ctx, String raw) {
        String low = lower(raw);

        if (low.contains("error de red") || low.contains("network")
                || low.contains("timeout") || low.contains("unable to resolve")) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_network));
        }
        // Email duplicado
        if (low.contains("already been taken")
                || low.contains("ya está registrado")
                || low.contains("duplicate entry")
                || low.contains("unique constraint")) {
            // Comprobar DNI.
            if (low.contains("dni")) {
                return new Resultado(Campo.DNI,
                        ctx.getString(R.string.err_dni_already_taken));
            }
            return new Resultado(Campo.EMAIL,
                    ctx.getString(R.string.err_email_already_taken));
        }
        // Validación email
        if (low.contains("email") && (low.contains("invalid") || low.contains("must") || low.contains("rfc") || low.contains("dns"))) {
            return new Resultado(Campo.EMAIL, ctx.getString(R.string.err_email_invalid));
        }
        if (low.contains("password") && low.contains("min")) {
            return new Resultado(Campo.PASSWORD, ctx.getString(R.string.err_password_too_short));
        }
        if (low.contains("password") && low.contains("confirmed")) {
            return new Resultado(Campo.PASSWORD, ctx.getString(R.string.err_passwords_dont_match));
        }
        if (low.contains("sqlstate") || low.contains("server error")
                || low.contains("internal server")) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_server));
        }
        if (raw == null || raw.isEmpty() || looksLikeStackTrace(raw)) {
            return new Resultado(Campo.GENERAL, ctx.getString(R.string.err_unknown));
        }
        return new Resultado(Campo.GENERAL, raw);
    }

    public static String paraGenerico(Context ctx, String raw) {
        String low = lower(raw);
        if (low.contains("error de red") || low.contains("network")
                || low.contains("timeout") || low.contains("unable to resolve")) {
            return ctx.getString(R.string.err_network);
        }
        if (low.contains("unauthorized") || low.contains("401")) {
            return ctx.getString(R.string.err_unauthorized);
        }
        if (low.contains("sqlstate") || looksLikeStackTrace(raw)) {
            return ctx.getString(R.string.err_server);
        }
        return raw == null || raw.isEmpty() ? ctx.getString(R.string.err_unknown) : raw;
    }

    // ----------------------------------------------------------------- helpers

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static boolean looksLikeStackTrace(String s) {
        return s.contains("\n#") || s.contains("Stack trace")
                || s.length() > 300;
    }
}
