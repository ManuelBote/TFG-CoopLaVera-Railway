package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.Finca;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class FincaAdapter extends BaseAdapter {

    public interface OnFincaAction {
        void onEditar(Finca finca);
        void onEliminar(Finca finca);
    }

    private final List<Finca> items;
    private final OnFincaAction listener;

    public FincaAdapter(List<Finca> items, OnFincaAction listener) {
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
                    .inflate(R.layout.item_finca, parent, false);
        }

        Finca f = items.get(position);

        TextView titulo = fila.findViewById(R.id.tvFincaTitulo);
        TextView localidad = fila.findViewById(R.id.tvFincaLocalidad);
        TextView direccion = fila.findViewById(R.id.tvFincaDireccion);
        TextView arboles = fila.findViewById(R.id.tvFincaArboles);
        MaterialButton btnEditar = fila.findViewById(R.id.btnEditarFinca);
        MaterialButton btnEliminar = fila.findViewById(R.id.btnEliminarFinca);

        titulo.setText(parent.getContext().getString(R.string.finca_titulo_format, f.getId()));
        localidad.setText(parent.getContext().getString(R.string.finca_localidad_format, f.getLocalidad()));
        direccion.setText(parent.getContext().getString(R.string.finca_direccion_format, f.getDireccion()));
        arboles.setText(String.format(Locale.getDefault(),
                "Higueras: %d   Ciruelos: %d\nArándanos: %d   Cerezos: %d",
                f.getHigueras(), f.getCiruelos(), f.getArandanos(), f.getCerezos()));

        btnEditar.setOnClickListener(v -> listener.onEditar(f));
        btnEliminar.setOnClickListener(v -> listener.onEliminar(f));
        return fila;
    }
}
