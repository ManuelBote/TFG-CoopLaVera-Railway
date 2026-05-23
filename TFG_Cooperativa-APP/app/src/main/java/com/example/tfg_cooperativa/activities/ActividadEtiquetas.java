package com.example.tfg_cooperativa.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.CatalogoApi;
import com.example.tfg_cooperativa.api.EtiquetasApi;
import com.example.tfg_cooperativa.api.FincaApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.models.Finca;
import com.example.tfg_cooperativa.models.Producto;
import com.example.tfg_cooperativa.session.GestorSesion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vista de Etiquetas para el socio. Elige una finca, va añadiendo
 * producto + cantidad a una lista y al "Generar" envía las etiquetas al back
 * ({@code POST /etiquetas}). Esas etiquetas son el cupo que luego le permite
 * entregar fruta. Replica el flujo de la web (Frontend/Etiquetas).
 */
public class ActividadEtiquetas extends ActividadBase {

    /** Columnas de la tabla etiquetas, por orden de fruta. */
    private static final String[] COLUMNAS =
            {"higo_fresco", "higo_seco", "arandano", "cereza", "ciruela"};

    /** Una línea añadida: producto + cantidad de etiquetas. */
    private static class Linea {
        final String nombre;
        final int cantidad;
        Linea(String nombre, int cantidad) { this.nombre = nombre; this.cantidad = cantidad; }
    }

    private Spinner spinnerFinca;
    private Spinner spinnerProducto;
    private TextInputEditText etCantidad;
    private LinearLayout contenedorLista;
    private TextView tvVacio;
    private MaterialButton btnGenerar;

    private final List<Finca> fincas = new ArrayList<>();
    private final List<Producto> productos = new ArrayList<>();
    private final List<Linea> lineas = new ArrayList<>();

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_etiquetas;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuEtiquetas;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spinnerFinca = findViewById(R.id.spinnerEtiFinca);
        spinnerProducto = findViewById(R.id.spinnerEtiProducto);
        etCantidad = findViewById(R.id.etEtiCantidad);
        contenedorLista = findViewById(R.id.contenedorEtiLista);
        tvVacio = findViewById(R.id.tvEtiVacio);
        btnGenerar = findViewById(R.id.btnEtiGenerar);

        findViewById(R.id.btnEtiAnadir).setOnClickListener(v -> anadirLinea());
        btnGenerar.setOnClickListener(v -> generar());

        refrescarLista();
        cargarFincas();
        cargarProductos();
    }

    // -------------------------------------------------- carga de datos

    private void cargarFincas() {
        FincaApi.misFincas(this, new ApiCallback<List<Finca>>() {
            @Override public void onSuccess(List<Finca> list) {
                if (estaInactiva()) return;
                fincas.clear();
                fincas.addAll(list);
                List<String> etiquetas = new ArrayList<>();
                for (Finca f : fincas) {
                    etiquetas.add(getString(R.string.finca_titulo_format, f.getId())
                            + " - " + f.getLocalidad());
                }
                rellenarSpinner(spinnerFinca, etiquetas);
                if (fincas.isEmpty()) {
                    Toast.makeText(ActividadEtiquetas.this, R.string.etiquetas_sin_fincas,
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadEtiquetas.this,
                        MapeadorErrores.paraGenerico(ActividadEtiquetas.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarProductos() {
        CatalogoApi.listProductos(this, new ApiCallback<List<Producto>>() {
            @Override public void onSuccess(List<Producto> list) {
                if (estaInactiva()) return;
                productos.clear();
                productos.addAll(list);
                List<String> nombres = new ArrayList<>();
                for (Producto p : productos) nombres.add(p.getName());
                rellenarSpinner(spinnerProducto, nombres);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadEtiquetas.this,
                        MapeadorErrores.paraGenerico(ActividadEtiquetas.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rellenarSpinner(Spinner spinner, List<String> datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, datos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // -------------------------------------------------- lista de líneas

    private void anadirLinea() {
        int pos = spinnerProducto.getSelectedItemPosition();
        if (pos < 0 || pos >= productos.size()) {
            Toast.makeText(this, R.string.etiquetas_err_campos, Toast.LENGTH_SHORT).show();
            return;
        }
        int cantidad = enteroDe(etCantidad);
        if (cantidad <= 0) {
            Toast.makeText(this, R.string.etiquetas_err_campos, Toast.LENGTH_SHORT).show();
            return;
        }
        lineas.add(new Linea(productos.get(pos).getName(), cantidad));
        etCantidad.setText("");
        refrescarLista();
    }

    private void refrescarLista() {
        contenedorLista.removeAllViews();
        for (int i = 0; i < lineas.size(); i++) {
            final int indice = i;
            Linea l = lineas.get(i);

            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int pad = (int) (8 * getResources().getDisplayMetrics().density);
            fila.setPadding(0, pad, 0, pad);

            TextView texto = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            texto.setLayoutParams(lp);
            texto.setText(String.format(Locale.getDefault(), "%s  ×  %d", l.nombre, l.cantidad));
            texto.setTextColor(getResources().getColor(R.color.gray_800, getTheme()));

            MaterialButton quitar = new MaterialButton(this);
            quitar.setText("✕");
            quitar.setOnClickListener(v -> { lineas.remove(indice); refrescarLista(); });

            fila.addView(texto);
            fila.addView(quitar);
            contenedorLista.addView(fila);
        }
        boolean vacio = lineas.isEmpty();
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        btnGenerar.setEnabled(!vacio);
    }

    // -------------------------------------------------- generar

    private void generar() {
        int posFinca = spinnerFinca.getSelectedItemPosition();
        if (posFinca < 0 || posFinca >= fincas.size()) {
            Toast.makeText(this, R.string.etiquetas_sin_fincas, Toast.LENGTH_SHORT).show();
            return;
        }
        if (lineas.isEmpty()) {
            Toast.makeText(this, R.string.etiquetas_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Sumamos las cantidades por columna (fruta).
        int[] cantidades = new int[COLUMNAS.length];
        for (Linea l : lineas) {
            int idx = columnaIndice(l.nombre);
            if (idx >= 0) cantidades[idx] += l.cantidad;
        }

        int idFinca = fincas.get(posFinca).getId();
        int idUsuario = GestorSesion.get(this).getUserId();

        btnGenerar.setEnabled(false);
        btnGenerar.setText(R.string.loading);

        EtiquetasApi.crear(this, idUsuario, idFinca,
                cantidades[0], cantidades[1], cantidades[2], cantidades[3], cantidades[4],
                new ApiCallback<Void>() {
                    @Override public void onSuccess(Void v) {
                        if (estaInactiva()) return;
                        Toast.makeText(ActividadEtiquetas.this, R.string.etiquetas_creadas_ok,
                                Toast.LENGTH_SHORT).show();
                        lineas.clear();
                        refrescarLista();
                        btnGenerar.setText(R.string.etiquetas_generar);
                    }
                    @Override public void onError(String message) {
                        if (estaInactiva()) return;
                        Toast.makeText(ActividadEtiquetas.this,
                                MapeadorErrores.paraGenerico(ActividadEtiquetas.this, message),
                                Toast.LENGTH_LONG).show();
                        btnGenerar.setEnabled(true);
                        btnGenerar.setText(R.string.etiquetas_generar);
                    }
                });
    }

    /** Mapea el nombre del producto a la columna de etiquetas (0..4), o -1. */
    private int columnaIndice(String nombre) {
        String n = Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        if (n.contains("higo") && n.contains("seco")) return 1;
        if (n.contains("higo")) return 0;
        if (n.contains("arandano")) return 2;
        if (n.contains("cereza")) return 3;
        if (n.contains("ciruela")) return 4;
        return -1;
    }

    private int enteroDe(TextInputEditText et) {
        String s = et.getText() == null ? "" : et.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException nfe) { return 0; }
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
