package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.SolicitudAlquiler;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SolicitudAdapter extends BaseAdapter {

    public interface OnSolicitudAction {
        void onAceptar(SolicitudAlquiler solicitud);
        void onRechazar(SolicitudAlquiler solicitud);
        void onDetalle(SolicitudAlquiler solicitud);
    }

    private static final SimpleDateFormat FORMATO_FECHA =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    private final List<SolicitudAlquiler> items;
    private final OnSolicitudAction listener;

    public SolicitudAdapter(List<SolicitudAlquiler> items, OnSolicitudAction listener) {
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
                    .inflate(R.layout.item_solicitud, parent, false);
        }

        SolicitudAlquiler s = items.get(position);

        TextView estado = fila.findViewById(R.id.tvSolicitudEstado);
        TextView fecha = fila.findViewById(R.id.tvSolicitudFecha);
        TextView usuario = fila.findViewById(R.id.tvSolicitudUsuario);
        TextView detalles = fila.findViewById(R.id.tvSolicitudDetalles);
        View acciones = fila.findViewById(R.id.layoutSolicitudAcciones);
        MaterialButton btnAceptar = fila.findViewById(R.id.btnAceptar);
        MaterialButton btnRechazar = fila.findViewById(R.id.btnRechazar);

        usuario.setText(s.getNombreUsuario());
        fecha.setText(s.getFecha() != null ? FORMATO_FECHA.format(s.getFecha()) : "");

        int estadoRes;
        int bgRes;
        int colorRes;
        if (s.esAceptada()) {
            estadoRes = R.string.solicitud_estado_aceptada;
            bgRes = R.drawable.bg_chip_entrega;
            colorRes = R.color.green_700;
        } else if (s.esRechazada()) {
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

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.getDetalles().size(); i++) {
            if (i > 0) sb.append("\n");
            SolicitudAlquiler.Linea linea = s.getDetalles().get(i);
            sb.append(String.format(Locale.getDefault(), "%s × %d",
                    linea.getNombreMaterial(), linea.getCantidad()));
        }
        detalles.setText(sb.toString());

        if (s.esPendiente()) {
            acciones.setVisibility(View.VISIBLE);
            btnAceptar.setOnClickListener(v -> listener.onAceptar(s));
            btnRechazar.setOnClickListener(v -> listener.onRechazar(s));
        } else {
            acciones.setVisibility(View.GONE);
        }

        fila.setOnClickListener(v -> listener.onDetalle(s));
        return fila;
    }
}
