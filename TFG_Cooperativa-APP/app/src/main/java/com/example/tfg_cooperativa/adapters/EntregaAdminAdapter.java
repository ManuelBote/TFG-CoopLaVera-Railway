package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
        void onAceptar(EntregaAdmin entrega);
        void onRechazar(EntregaAdmin entrega);
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
        TextView estado = fila.findViewById(R.id.tvEntregaEstado);
        TextView socio = fila.findViewById(R.id.tvEntregaSocio);
        TextView calibres = fila.findViewById(R.id.tvEntregaCalibres);
        TextView total = fila.findViewById(R.id.tvEntregaTotal);
        View accionesEstado = fila.findViewById(R.id.layoutEntregaEstadoAcciones);
        MaterialButton btnAceptar = fila.findViewById(R.id.btnAceptarEntrega);
        MaterialButton btnRechazar = fila.findViewById(R.id.btnRechazarEntrega);
        MaterialButton btnEditar = fila.findViewById(R.id.btnEditarEntrega);
        MaterialButton btnEliminar = fila.findViewById(R.id.btnEliminarEntrega);

        producto.setText(e.getNombreProducto());
        fecha.setText(formatearFecha(e.getFechaEntrega()));
        socio.setText(String.format(Locale.getDefault(), "Socio #%d", e.getIdUsuario()));
        calibres.setText(String.format(Locale.getDefault(),
                "Congelado: %d   M: %d   L: %d   Jumbo: %d",
                e.getCCongelados(), e.getCM(), e.getCL(), e.getCJumbo()));
        total.setText(String.format(Locale.getDefault(), "Total: %d kg", e.getTotal()));

        // Chip de estado
        int estadoRes, bgRes, colorRes;
        if (e.esAceptado()) {
            estadoRes = R.string.solicitud_estado_aceptada;
            bgRes = R.drawable.bg_chip_entrega;
            colorRes = R.color.green_700;
        } else if (e.esRechazado()) {
            estadoRes = R.string.solicitud_estado_rechazada;
            bgRes = R.drawable.bg_chip_rechazada;
            colorRes = R.color.red_700;
        } else {
            estadoRes = R.string.solicitud_estado_pendiente;
            bgRes = R.drawable.bg_chip_pedido;
            colorRes = R.color.yellow_700;
        }
        estado.setText(estadoRes);
        estado.setBackgroundResource(bgRes);
        estado.setTextColor(ContextCompat.getColor(parent.getContext(), colorRes));

        // Aceptar/rechazar solo en pendientes
        if (e.esPendiente()) {
            accionesEstado.setVisibility(View.VISIBLE);
            btnAceptar.setOnClickListener(v -> listener.onAceptar(e));
            btnRechazar.setOnClickListener(v -> listener.onRechazar(e));
        } else {
            accionesEstado.setVisibility(View.GONE);
        }

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
