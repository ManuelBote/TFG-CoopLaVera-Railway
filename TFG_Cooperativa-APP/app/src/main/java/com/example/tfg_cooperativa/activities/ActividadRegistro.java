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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ActividadRegistro extends ActividadBase {

    private static final int MIN_PASSWORD = 6;
    private TextInputLayout tilName, tilEmail, tilPassword, tilPhone, tilDni;
    private TextInputEditText etName, etEmail, etPassword, etPhone, etDni;
    private MaterialButton btnRegister;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_registro;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPhone = findViewById(R.id.tilPhone);
        tilDni = findViewById(R.id.tilDni);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etDni = findViewById(R.id.etDni);

        btnRegister = findViewById(R.id.btnRegister);

        limpiarErrorAlEscribir(etName, tilName);
        limpiarErrorAlEscribir(etEmail, tilEmail);
        limpiarErrorAlEscribir(etPassword, tilPassword);
        limpiarErrorAlEscribir(etPhone, tilPhone);
        limpiarErrorAlEscribir(etDni, tilDni);

        btnRegister.setOnClickListener(v -> intentarRegistro());

        TextView enlaceLogin = findViewById(R.id.tvLoginLink);
        enlaceLogin.setOnClickListener(v -> abrir(ActividadLogin.class));
    }


    private void intentarRegistro() {
        String nombre = textoDe(etName);
        String email = textoDe(etEmail);
        String pass = textoDe(etPassword);
        String telefono = textoDe(etPhone);
        String dni = textoDe(etDni);

        if (!validarCliente(nombre, email, pass, telefono, dni)) return;

        setCargando(true);
        AutenticacionApi.register(this, nombre, "", dni, email, telefono, "", "", pass,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        if (estaInactiva()) return;
                        setCargando(false);
                        Toast.makeText(ActividadRegistro.this,
                                R.string.toast_register_ok, Toast.LENGTH_LONG).show();
                        abrir(ActividadLogin.class);
                    }

                    @Override
                    public void onError(String message) {
                        if (estaInactiva()) return;
                        setCargando(false);
                        mostrarErrorServidor(message);
                    }
                });
    }

    private boolean validarCliente(String nombre, String email, String pass,
                                   String telefono, String dni) {
        boolean ok = true;
        if (TextUtils.isEmpty(nombre)) {
            tilName.setError(getString(R.string.err_name_required));
            ok = false;
        }
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
        } else if (pass.length() < MIN_PASSWORD) {
            tilPassword.setError(getString(R.string.err_password_too_short));
            ok = false;
        }
        if (TextUtils.isEmpty(telefono)) {
            tilPhone.setError(getString(R.string.err_phone_required));
            ok = false;
        }
        if (TextUtils.isEmpty(dni)) {
            tilDni.setError(getString(R.string.err_dni_required));
            ok = false;
        }
        return ok;
    }

    private void mostrarErrorServidor(String raw) {
        MapeadorErrores.Resultado r = MapeadorErrores.paraRegistro(this, raw);
        switch (r.campo) {
            case EMAIL:
                tilEmail.setError(r.mensaje);
                etEmail.requestFocus();
                break;
            case PASSWORD:
                tilPassword.setError(r.mensaje);
                etPassword.requestFocus();
                break;
            case DNI:
                tilDni.setError(r.mensaje);
                etDni.requestFocus();
                break;
            default:
                Toast.makeText(this, r.mensaje, Toast.LENGTH_LONG).show();
                break;
        }
    }


    private void setCargando(boolean cargando) {
        btnRegister.setEnabled(!cargando);
        btnRegister.setText(cargando ? R.string.loading : R.string.register_button);
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
