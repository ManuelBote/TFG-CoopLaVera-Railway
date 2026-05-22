package com.example.tfg_cooperativa.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.adapters.PedidoAdapter;
import com.example.tfg_cooperativa.util.UtilLista;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.CatalogoApi;
import com.example.tfg_cooperativa.api.EntregasApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.api.PerfilApi;
import com.example.tfg_cooperativa.data.RepositorioProductos;
import com.example.tfg_cooperativa.models.Pedido;
import com.example.tfg_cooperativa.models.Producto;
import com.example.tfg_cooperativa.session.GestorSesion;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ActividadPortal extends ActividadBase {

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DIA_LABEL = new SimpleDateFormat("dd/MM", new Locale("es", "ES"));

    private View panelMisDatos, panelHistorial, panelGanancias;
    private MaterialButton btnTabMisDatos, btnTabHistorial, btnTabGanancias;

    private LinearLayout listaHistorial;
    private TextView tvSinRegistros;
    private ProgressBar progresoHistorial;
    private MaterialCardView cardGrafica;
    private BarChart graficaEntregas;

    private TextView tvGananciaTotal, tvGananciasVacio;
    private MaterialCardView cardGraficaGanancias;
    private LineChart graficaGanancias;
    private LinearLayout listaGananciasHistorial;

    private TextView tvNombre, tvEmail, tvTelefono, tvDni, tvBienvenida;

    private final List<Pedido> entregas = new ArrayList<>();
    private final Map<String, Float> entregasPorProducto = new LinkedHashMap<>();
    private final Map<String, Float> gananciasPorDia = new TreeMap<>();
    private double gananciaTotal = 0;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_portal;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuPortal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvBienvenida = findViewById(R.id.tvPortalWelcome);
        tvNombre = findViewById(R.id.tvDataName);
        tvEmail = findViewById(R.id.tvDataEmail);
        tvTelefono = findViewById(R.id.tvDataPhone);
        tvDni = findViewById(R.id.tvDataDni);

        panelMisDatos = findViewById(R.id.panelMyData);
        panelHistorial = findViewById(R.id.panelHistory);
        panelGanancias = findViewById(R.id.panelGanancias);
        btnTabMisDatos = findViewById(R.id.btnTabMyData);
        btnTabHistorial = findViewById(R.id.btnTabHistory);
        btnTabGanancias = findViewById(R.id.btnTabGanancias);

        listaHistorial = findViewById(R.id.rvHistory);
        tvSinRegistros = findViewById(R.id.tvNoRecords);
        progresoHistorial = findViewById(R.id.progressHistory);
        cardGrafica = findViewById(R.id.cardChart);
        graficaEntregas = findViewById(R.id.chartDeliveries);

        tvGananciaTotal = findViewById(R.id.tvGananciaTotal);
        tvGananciasVacio = findViewById(R.id.tvGananciasEmpty);
        cardGraficaGanancias = findViewById(R.id.cardGananciasChart);
        graficaGanancias = findViewById(R.id.chartGanancias);
        listaGananciasHistorial = findViewById(R.id.rvGananciasHistorial);

        pintarSesionLocal();

        findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> mostrarDialogoEditarPerfil());

        btnTabHistorial.setOnClickListener(v -> mostrarHistorial());
        btnTabGanancias.setOnClickListener(v -> mostrarGanancias());
        btnTabMisDatos.setOnClickListener(v -> mostrarMisDatos());
        mostrarHistorial();

        cargarPerfil();
        cargarProductosYEntregas();
    }

    private void pintarSesionLocal() {
        GestorSesion s = GestorSesion.get(this);
        tvBienvenida.setText(getString(R.string.portal_welcome_format, s.getName()));
        tvNombre.setText(s.getName());
        tvEmail.setText(s.getEmail());
        tvTelefono.setText(s.getPhone());
        tvDni.setText(s.getDni());
    }

    private void cargarPerfil() {
        PerfilApi.getPerfil(this, new ApiCallback<JSONObject>() {
            @Override public void onSuccess(JSONObject user) {
                if (estaInactiva()) return;
                pintarSesionLocal();
            }
            @Override public void onError(String message) { /* dejamos los datos locales */ }
        });
    }


    private void mostrarDialogoEditarPerfil() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_editar_perfil, null, false);

        GestorSesion s = GestorSesion.get(this);
        TextInputEditText etNombre = content.findViewById(R.id.etPerfilNombre);
        TextInputEditText etTelefono = content.findViewById(R.id.etPerfilTelefono);
        TextInputEditText etDni = content.findViewById(R.id.etPerfilDni);

        etNombre.setText(s.getName());
        etTelefono.setText(s.getPhone());
        etDni.setText(s.getDni());

        final String email = s.getEmail();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.profile_dialog_title)
                .setView(content)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = textoDe(etNombre);
            String telefono = textoDe(etTelefono);
            String dni = textoDe(etDni);

            if (nombre.isEmpty()) {
                Toast.makeText(this, R.string.error_register_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            PerfilApi.actualizarPerfil(this, nombre, email, telefono, dni,
                    new ApiCallback<Void>() {
                        @Override public void onSuccess(Void unused) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadPortal.this, R.string.profile_updated_ok,
                                    Toast.LENGTH_SHORT).show();
                            pintarSesionLocal();
                            dialog.dismiss();
                        }
                        @Override public void onError(String message) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadPortal.this,
                                    MapeadorErrores.paraGenerico(ActividadPortal.this, message),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }));

        dialog.show();
    }

    private String textoDe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }


    private void cargarProductosYEntregas() {
        if (!RepositorioProductos.get().getProduce().isEmpty()) {
            cargarEntregas();
            return;
        }
        CatalogoApi.listProductos(this, new ApiCallback<List<Producto>>() {
            @Override public void onSuccess(List<Producto> list) { cargarEntregas(); }
            @Override public void onError(String message) { cargarEntregas(); }
        });
    }

    private void cargarEntregas() {
        progresoHistorial.setVisibility(View.VISIBLE);
        listaHistorial.setVisibility(View.GONE);
        tvSinRegistros.setVisibility(View.GONE);

        EntregasApi.entregasUsuario(this, new ApiCallback<JSONObject>() {
            @Override public void onSuccess(JSONObject agrupadas) {
                if (estaInactiva()) return;
                progresoHistorial.setVisibility(View.GONE);
                parsearEntregas(agrupadas);
                refrescarHistorial();
                montarGraficaCantidades();
                montarGanancias();
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                progresoHistorial.setVisibility(View.GONE);
                cardGrafica.setVisibility(View.GONE);
                listaHistorial.setVisibility(View.GONE);
                tvSinRegistros.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadPortal.this, message)));
                tvSinRegistros.setVisibility(View.VISIBLE);
                montarGanancias();
            }
        });
    }

    private void parsearEntregas(JSONObject agrupadas) {
        entregas.clear();
        entregasPorProducto.clear();
        gananciasPorDia.clear();
        gananciaTotal = 0;
        if (agrupadas == null) return;

        Iterator<String> claves = agrupadas.keys();
        long idSintetico = 100000L;
        while (claves.hasNext()) {
            String nombreProducto = claves.next();
            JSONArray filas = agrupadas.optJSONArray(nombreProducto);
            if (filas == null) continue;

            double precioMedio = precioMedioProducto(nombreProducto);
            float totalCantidad = 0f;

            for (int i = 0; i < filas.length(); i++) {
                JSONObject fila = filas.optJSONObject(i);
                if (fila == null) continue;
                int cantidad = fila.optInt("cantidad", 0);
                Date fecha = parsearFecha(fila.optString("fecha", ""));
                double importe = cantidad * precioMedio;

                Pedido.OrderLine linea = new Pedido.OrderLine(nombreProducto, cantidad);
                entregas.add(new Pedido(idSintetico++, Pedido.Type.ENTREGA, fecha,
                        Collections.singletonList(linea), importe, Pedido.Status.COMPLETED));

                totalCantidad += cantidad;
                gananciaTotal += importe;

                if (fecha != null) {
                    String dia = ISO.format(fecha);
                    Float acum = gananciasPorDia.get(dia);
                    gananciasPorDia.put(dia, (acum == null ? 0f : acum) + (float) importe);
                }
            }
            entregasPorProducto.put(nombreProducto, totalCantidad);
        }
    }

    private double precioMedioProducto(String nombre) {
        Producto p = null;
        for (Producto candidato : RepositorioProductos.get().getProduce()) {
            if (candidato.getName().equalsIgnoreCase(nombre)) { p = candidato; break; }
        }
        if (p == null) return 0;
        Double[] precios = {p.getPriceCongelado(), p.getPriceM(), p.getPriceL(), p.getPriceJumbo()};
        double suma = 0; int n = 0;
        for (Double pr : precios) {
            if (pr != null && pr > 0) { suma += pr; n++; }
        }
        return n > 0 ? suma / n : 0;
    }

    private void refrescarHistorial() {
        List<Pedido> todas = entregasOrdenadas();
        UtilLista.rellenar(listaHistorial, new PedidoAdapter(todas, this::mostrarDetalleEntrega));
        UtilLista.rellenar(listaGananciasHistorial,
                new PedidoAdapter(new ArrayList<>(todas), this::mostrarDetalleEntrega));

        if (todas.isEmpty()) {
            tvSinRegistros.setVisibility(View.VISIBLE);
            listaHistorial.setVisibility(View.GONE);
        } else {
            tvSinRegistros.setVisibility(View.GONE);
            listaHistorial.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarDetalleEntrega(Pedido p) {
        String producto = p.getLines().isEmpty() ? "-" : p.getLines().get(0).getName();
        int cantidad = p.getLines().isEmpty() ? 0 : p.getLines().get(0).getQuantity();
        String fecha = p.getDate() != null
                ? new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).format(p.getDate())
                : "-";

        String msg = "Producto: " + producto
                + "\nFecha: " + fecha
                + "\nCantidad: " + cantidad + " kg"
                + "\nImporte estimado: " + String.format(Locale.getDefault(), "€%.2f", p.getTotal());

        new AlertDialog.Builder(this)
                .setTitle(R.string.detalle_entrega_title)
                .setMessage(msg)
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    private List<Pedido> entregasOrdenadas() {
        List<Pedido> todas = new ArrayList<>(entregas);
        todas.sort((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });
        return todas;
    }


    private void mostrarHistorial() {
        panelHistorial.setVisibility(View.VISIBLE);
        panelGanancias.setVisibility(View.GONE);
        panelMisDatos.setVisibility(View.GONE);
        estiloPestania(btnTabHistorial, true);
        estiloPestania(btnTabGanancias, false);
        estiloPestania(btnTabMisDatos, false);
    }

    private void mostrarGanancias() {
        panelHistorial.setVisibility(View.GONE);
        panelGanancias.setVisibility(View.VISIBLE);
        panelMisDatos.setVisibility(View.GONE);
        estiloPestania(btnTabHistorial, false);
        estiloPestania(btnTabGanancias, true);
        estiloPestania(btnTabMisDatos, false);
    }

    private void mostrarMisDatos() {
        panelHistorial.setVisibility(View.GONE);
        panelGanancias.setVisibility(View.GONE);
        panelMisDatos.setVisibility(View.VISIBLE);
        estiloPestania(btnTabHistorial, false);
        estiloPestania(btnTabGanancias, false);
        estiloPestania(btnTabMisDatos, true);
    }

    private void estiloPestania(MaterialButton btn, boolean activa) {
        if (activa) {
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.green_600, getTheme()));
            btn.setTextColor(getResources().getColor(R.color.white, getTheme()));
            btn.setStrokeWidth(0);
        } else {
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.white, getTheme()));
            btn.setTextColor(getResources().getColor(R.color.green_700, getTheme()));
            btn.setStrokeColor(getResources().getColorStateList(R.color.green_600, getTheme()));
            btn.setStrokeWidth(2);
        }
    }


    private void montarGraficaCantidades() {
        if (entregasPorProducto.isEmpty()) {
            cardGrafica.setVisibility(View.GONE);
            return;
        }
        cardGrafica.setVisibility(View.VISIBLE);

        List<BarEntry> entradas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> e : entregasPorProducto.entrySet()) {
            entradas.add(new BarEntry(i++, e.getValue()));
            etiquetas.add(e.getKey());
        }

        BarDataSet conjunto = new BarDataSet(entradas, getString(R.string.portal_chart_subtitle));
        conjunto.setColor(ContextCompat.getColor(this, R.color.green_600));
        conjunto.setValueTextColor(ContextCompat.getColor(this, R.color.gray_700));
        conjunto.setValueTextSize(11f);
        conjunto.setValueFormatter(new ValueFormatter() {
            @Override public String getBarLabel(BarEntry barEntry) {
                return String.format(Locale.getDefault(), "%.0f kg", barEntry.getY());
            }
        });

        BarData data = new BarData(conjunto);
        data.setBarWidth(0.6f);
        graficaEntregas.setData(data);

        XAxis ejeX = graficaEntregas.getXAxis();
        ejeX.setPosition(XAxis.XAxisPosition.BOTTOM);
        ejeX.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        ejeX.setGranularity(1f);
        ejeX.setDrawGridLines(false);
        ejeX.setTextColor(ContextCompat.getColor(this, R.color.gray_700));
        ejeX.setLabelRotationAngle(-30f);

        YAxis ejeIzq = graficaEntregas.getAxisLeft();
        ejeIzq.setAxisMinimum(0f);
        ejeIzq.setTextColor(ContextCompat.getColor(this, R.color.gray_500));
        ejeIzq.setGridColor(ContextCompat.getColor(this, R.color.gray_200));

        graficaEntregas.getAxisRight().setEnabled(false);
        graficaEntregas.getLegend().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        graficaEntregas.setDescription(desc);

        graficaEntregas.setFitBars(true);
        graficaEntregas.setNoDataText("");
        graficaEntregas.setExtraBottomOffset(8f);
        graficaEntregas.setDrawGridBackground(false);
        graficaEntregas.setBackgroundColor(Color.TRANSPARENT);
        graficaEntregas.animateY(600);
        graficaEntregas.invalidate();
    }

    private void montarGanancias() {
        tvGananciaTotal.setText(String.format(Locale.getDefault(), "€%.2f", gananciaTotal));

        if (gananciasPorDia.isEmpty()) {
            cardGraficaGanancias.setVisibility(View.GONE);
            tvGananciasVacio.setVisibility(View.VISIBLE);
            return;
        }
        cardGraficaGanancias.setVisibility(View.VISIBLE);
        tvGananciasVacio.setVisibility(View.GONE);

        List<Entry> entradas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> e : gananciasPorDia.entrySet()) {
            entradas.add(new Entry(i++, e.getValue()));
            etiquetas.add(etiquetaDia(e.getKey()));
        }

        LineDataSet conjunto = new LineDataSet(entradas, getString(R.string.portal_ganancias_chart_title));
        conjunto.setColor(ContextCompat.getColor(this, R.color.green_600));
        conjunto.setLineWidth(2.5f);
        conjunto.setCircleColor(ContextCompat.getColor(this, R.color.green_700));
        conjunto.setCircleRadius(4f);
        conjunto.setDrawCircleHole(false);
        conjunto.setMode(LineDataSet.Mode.LINEAR);
        conjunto.setDrawValues(false);
        conjunto.setDrawFilled(true);
        conjunto.setFillColor(ContextCompat.getColor(this, R.color.green_200));

        graficaGanancias.setData(new LineData(conjunto));

        XAxis ejeX = graficaGanancias.getXAxis();
        ejeX.setPosition(XAxis.XAxisPosition.BOTTOM);
        ejeX.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        ejeX.setGranularity(1f);
        ejeX.setDrawGridLines(false);
        ejeX.setTextColor(ContextCompat.getColor(this, R.color.gray_700));
        ejeX.setLabelRotationAngle(-30f);

        YAxis ejeIzq = graficaGanancias.getAxisLeft();
        ejeIzq.setAxisMinimum(0f);
        ejeIzq.setTextColor(ContextCompat.getColor(this, R.color.gray_500));
        ejeIzq.setGridColor(ContextCompat.getColor(this, R.color.gray_200));

        graficaGanancias.getAxisRight().setEnabled(false);
        graficaGanancias.getLegend().setEnabled(false);

        Description desc = new Description();
        desc.setText("");
        graficaGanancias.setDescription(desc);

        graficaGanancias.setNoDataText("");
        graficaGanancias.setExtraBottomOffset(8f);
        graficaGanancias.setDrawGridBackground(false);
        graficaGanancias.setBackgroundColor(Color.TRANSPARENT);
        graficaGanancias.animateX(600);
        graficaGanancias.invalidate();
    }

    private String etiquetaDia(String iso) {
        try {
            Date d = ISO.parse(iso);
            return d != null ? DIA_LABEL.format(d) : iso;
        } catch (ParseException e) {
            return iso;
        }
    }

    private Date parsearFecha(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String cabeza = raw.length() >= 10 ? raw.substring(0, 10) : raw;
        try {
            return ISO.parse(cabeza);
        } catch (ParseException pe) {
            return null;
        }
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
