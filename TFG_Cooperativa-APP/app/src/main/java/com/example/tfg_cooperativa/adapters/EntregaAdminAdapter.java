package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.EntregaAdmin;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntregaAdminAdapter extends BaseAdapter {

    public interface OnEntregaAction {
        void onEditar(EntregaAdmin entrega);
        void onEliminar(EntregaAdmin entrega);
        void onDetalle(EntregaAdmin entrega);
    }

    private static final SimpleDateFormat ENTRADA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat SALIDA = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    private final List<EntregaAdmin> items;
    private final OnEntregaAction listener;

    public EntregaAdminAdapter(List<EntregaAdmin> items, OnEntregaAction listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View fila = convertView;
        if (fila == null) {
            fila = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_entrega_admin, parent, false);
        }

        EntregaAdmin e = items.get(position);

        TextView producto = fila.findViewById(R.id.tvEntregaProducto);
        TextView fecha = fila.findViewById(R.id.tvEntregaFecha);
        TextView socio = fila.findViewById(R.id.tvEntregaSocio);
        TextView calibres = fila.findViewById(R.id.tvEntregaCalibres);
        TextView total = fila.findViewById(R.id.tvEntregaTotal);
        MaterialButton btnEditar = fila.findViewById(R.id.btnEditarEntrega);
        MaterialButton btnEliminar = fila.findViewById(R.id.btnEliminarEntrega);

        producto.setText(e.getNombreProducto());
        fecha.setText(formatearFecha(e.getFechaEntrega()));
        socio.setText(String.format(Locale.getDefault(), "Socio #%d", e.getIdUsuario()));
        calibres.setText(String.format(Locale.getDefault(),
                "Congelado: %d   M: %d   L: %d   Jumbo: %d",
                e.getCCongelados(), e.getCM(), e.getCL(), e.getCJumbo()));
        total.setText(String.format(Locale.getDefault(), "Total: %d kg", e.getTotal()));

        btnEditar.setOnClickListener(v -> listener.onEditar(e));
        btnEliminar.setOnClickListener(v -> listener.onEliminar(e));
        fila.setOnClickListener(v -> listener.onDetalle(e));
        return fila;
    }

    private String formatearFecha(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            String cabeza = raw.length() >= 19 ? raw.substring(0, 19) : raw;
            Date d = ENTRADA.parse(cabeza);
            return d != null ? SALIDA.format(d) : raw;
        } catch (ParseException ex) {
            return raw.length() >= 10 ? raw.substring(0, 10) : raw;
        }
    }
}
