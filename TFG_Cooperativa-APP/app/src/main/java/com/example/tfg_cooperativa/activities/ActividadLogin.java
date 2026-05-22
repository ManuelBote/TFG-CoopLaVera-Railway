package com.example.tfg_cooperativa.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.AutenticacionApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.session.GestorSesion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ActividadLogin extends ActividadBase {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_login;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuLogin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        limpiarErrorAlEscribir(etEmail, tilEmail);
        limpiarErrorAlEscribir(etPassword, tilPassword);

        btnLogin.setOnClickListener(v -> intentarLogin());

        TextView enlaceRegistro = findViewById(R.id.tvRegisterLink);
        enlaceRegistro.setOnClickListener(v -> abrir(ActividadRegistro.class));
    }


    private void intentarLogin() {
        String email = textoDe(etEmail);
        String pass = textoDe(etPassword);

        if (!validarCliente(email, pass)) return;

        setCargando(true);
        AutenticacionApi.login(this, email, pass, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                if (estaInactiva()) return;
                setCargando(false);
                String nombre = GestorSesion.get(ActividadLogin.this).getName();
                Toast.makeText(ActividadLogin.this,
                        getString(R.string.toast_login_ok, nombre),
                        Toast.LENGTH_SHORT).show();
                abrir(ActividadPortal.class);
            }

            @Override
            public void onError(String message) {
                if (estaInactiva()) return;
                setCargando(false);
                mostrarErrorServidor(message);
            }
        });
    }

    private boolean validarCliente(String email, String pass) {
        boolean ok = true;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.err_email_required));
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_email_invalid));
            ok = false;
        }
        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError(getString(R.string.err_password_required));
            ok = false;
        }
        return ok;
    }

    private void mostrarErrorServidor(String raw) {
        MapeadorErrores.Resultado r = MapeadorErrores.paraLogin(this, raw);
        switch (r.campo) {
            case EMAIL:
                tilEmail.setError(r.mensaje);
                etEmail.requestFocus();
                break;
            case PASSWORD:
                tilPassword.setError(r.mensaje);
                etPassword.requestFocus();
                break;
            default:
                Toast.makeText(this, r.mensaje, Toast.LENGTH_LONG).show();
                break;
        }
    }


    private void setCargando(boolean cargando) {
        btnLogin.setEnabled(!cargando);
        btnLogin.setText(cargando ? R.string.loading : R.string.login_button);
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }

    private static String textoDe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static void limpiarErrorAlEscribir(TextInputEditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                if (til.getError() != null) til.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
