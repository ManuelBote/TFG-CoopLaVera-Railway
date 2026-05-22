package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.UsuarioAdmin;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UsuarioAdminAdapter extends BaseAdapter {

    public interface OnUsuarioAction {
        void onAceptar(UsuarioAdmin usuario);
        void onRechazar(UsuarioAdmin usuario);
        void onDetalle(UsuarioAdmin usuario);
        void onEliminar(UsuarioAdmin usuario);
    }

    private final List<UsuarioAdmin> items;
    private final OnUsuarioAction listener;

    public UsuarioAdminAdapter(List<UsuarioAdmin> items, OnUsuarioAction listener) {
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
                    .inflate(R.layout.item_usuario_admin, parent, false);
        }

        UsuarioAdmin u = items.get(position);

        TextView nombre = fila.findViewById(R.id.tvUsuarioNombre);
        TextView email = fila.findViewById(R.id.tvUsuarioEmail);
        TextView estado = fila.findViewById(R.id.tvUsuarioEstado);
        View acciones = fila.findViewById(R.id.layoutUsuarioAcciones);
        MaterialButton btnAceptar = fila.findViewById(R.id.btnAceptarUsuario);
        MaterialButton btnRechazar = fila.findViewById(R.id.btnRechazarUsuario);

        nombre.setText(u.getNombreCompleto());
        email.setText(u.getEmail());

        int estadoRes;
        int bgRes;
        int colorRes;
        if (u.esAceptado()) {
            estadoRes = R.string.usuario_estado_aceptado;
            bgRes = R.drawable.bg_chip_entrega;
            colorRes = R.color.green_700;
        } else if (u.esRechazado()) {
            estadoRes = R.string.usuario_estado_rechazado;
            bgRes = R.drawable.bg_chip_rechazada;
            colorRes = R.color.red_700;
        } else {
            estadoRes = R.string.usuario_estado_pendiente;
            bgRes = R.drawable.bg_chip_pedido;
            colorRes = R.color.yellow_700;
        }
        estado.setText(estadoRes);
        estado.setBackgroundResource(bgRes);
        estado.setTextColor(ContextCompat.getColor(parent.getContext(), colorRes));

        if (u.esPendiente()) {
            acciones.setVisibility(View.VISIBLE);
            btnAceptar.setOnClickListener(v -> listener.onAceptar(u));
            btnRechazar.setOnClickListener(v -> listener.onRechazar(u));
        } else {
            acciones.setVisibility(View.GONE);
        }

        fila.setOnClickListener(v -> listener.onDetalle(u));

        // Solo los rechazados se pueden eliminar (mantener pulsado).
        if (u.esRechazado()) {
            fila.setOnLongClickListener(v -> {
                listener.onEliminar(u);
                return true;
            });
        } else {
            fila.setOnLongClickListener(null);
            fila.setLongClickable(false);
        }
        return fila;
    }
}
