package com.example.tfg_cooperativa.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.adapters.FincaAdapter;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.FincaApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.models.Finca;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ActividadMisFincas extends ActividadBase {

    private ListView listaFincas;
    private ProgressBar progreso;
    private TextView tvVacio;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_mis_fincas;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuFincas;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaFincas = findViewById(R.id.rvFincas);
        progreso = findViewById(R.id.progressFincas);
        tvVacio = findViewById(R.id.tvFincasEmpty);

        findViewById(R.id.btnNuevaFinca).setOnClickListener(v -> mostrarDialogo(null));

        cargarFincas();
    }

    private void cargarFincas() {
        progreso.setVisibility(View.VISIBLE);
        listaFincas.setVisibility(View.GONE);
        tvVacio.setVisibility(View.GONE);

        FincaApi.misFincas(this, new ApiCallback<List<Finca>>() {
            @Override public void onSuccess(List<Finca> list) {
                if (estaInactiva()) return;
                progreso.setVisibility(View.GONE);
                listaFincas.setAdapter(new FincaAdapter(list, accionesFinca()));
                boolean vacio = list.isEmpty();
                tvVacio.setText(R.string.fincas_empty);
                tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                listaFincas.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                progreso.setVisibility(View.GONE);
                listaFincas.setAdapter(new FincaAdapter(new ArrayList<>(), accionesFinca()));
                tvVacio.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadMisFincas.this, message)));
                tvVacio.setVisibility(View.VISIBLE);
                listaFincas.setVisibility(View.GONE);
            }
        });
    }

    private FincaAdapter.OnFincaAction accionesFinca() {
        return new FincaAdapter.OnFincaAction() {
            @Override public void onEditar(Finca f) { mostrarDialogo(f); }
            @Override public void onEliminar(Finca f) { confirmarEliminar(f); }
        };
    }

    private void confirmarEliminar(Finca f) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete)
                .setMessage(getString(R.string.fincas_delete_confirm, f.getId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, w) ->
                        FincaApi.eliminar(this, f.getId(), new ApiCallback<Void>() {
                            @Override public void onSuccess(Void v) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadMisFincas.this, R.string.fincas_deleted_ok,
                                        Toast.LENGTH_SHORT).show();
                                cargarFincas();
                            }
                            @Override public void onError(String message) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadMisFincas.this,
                                        MapeadorErrores.paraGenerico(ActividadMisFincas.this, message),
                                        Toast.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }

    private void mostrarDialogo(@Nullable Finca finca) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_finca, null, false);

        TextInputEditText etLocalidad = content.findViewById(R.id.etFincaLocalidad);
        TextInputEditText etDireccion = content.findViewById(R.id.etFincaDireccion);
        TextInputEditText etHigueras = content.findViewById(R.id.etFincaHigueras);
        TextInputEditText etCiruelos = content.findViewById(R.id.etFincaCiruelos);
        TextInputEditText etArandanos = content.findViewById(R.id.etFincaArandanos);
        TextInputEditText etCerezos = content.findViewById(R.id.etFincaCerezos);

        boolean editar = finca != null;
        if (editar) {
            etLocalidad.setText(finca.getLocalidad());
            etDireccion.setText(finca.getDireccion());
            etHigueras.setText(String.valueOf(finca.getHigueras()));
            etCiruelos.setText(String.valueOf(finca.getCiruelos()));
            etArandanos.setText(String.valueOf(finca.getArandanos()));
            etCerezos.setText(String.valueOf(finca.getCerezos()));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editar ? R.string.fincas_dialog_edit_title : R.string.fincas_dialog_new_title)
                .setView(content)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(editar ? R.string.btn_save : R.string.btn_create, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String localidad = textoDe(etLocalidad);
            String direccion = textoDe(etDireccion);
            if (localidad.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(this, R.string.fincas_err_required, Toast.LENGTH_SHORT).show();
                return;
            }
            int higueras = enteroDe(etHigueras);
            int ciruelos = enteroDe(etCiruelos);
            int arandanos = enteroDe(etArandanos);
            int cerezos = enteroDe(etCerezos);

            ApiCallback<Void> cb = new ApiCallback<Void>() {
                @Override public void onSuccess(Void unused) {
                    if (estaInactiva()) return;
                    Toast.makeText(ActividadMisFincas.this,
                            editar ? R.string.fincas_updated_ok : R.string.fincas_created_ok,
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    cargarFincas();
                }
                @Override public void onError(String message) {
                    if (estaInactiva()) return;
                    Toast.makeText(ActividadMisFincas.this,
                            MapeadorErrores.paraGenerico(ActividadMisFincas.this, message),
                            Toast.LENGTH_LONG).show();
                }
            };

            if (editar) {
                FincaApi.actualizar(this, finca.getId(), localidad, direccion,
                        higueras, ciruelos, arandanos, cerezos, cb);
            } else {
                FincaApi.crear(this, localidad, direccion,
                        higueras, ciruelos, arandanos, cerezos, cb);
            }
        }));

        dialog.show();
    }

    private String textoDe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private int enteroDe(TextInputEditText et) {
        String s = textoDe(et);
        if (TextUtils.isEmpty(s)) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
