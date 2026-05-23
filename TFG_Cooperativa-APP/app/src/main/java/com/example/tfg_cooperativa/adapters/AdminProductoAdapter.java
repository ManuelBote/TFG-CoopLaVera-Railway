package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.Producto;
import com.example.tfg_cooperativa.util.ImagenesProducto;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class AdminProductoAdapter extends BaseAdapter {

    public interface OnAdminAction {
        void onEdit(Producto product);
        void onDelete(Producto product);
    }

    private final List<Producto> items;
    private final OnAdminAction listener;
    private final boolean mostrarEditar;
    private final boolean mostrarEliminar;

    public AdminProductoAdapter(List<Producto> items, OnAdminAction listener) {
        this(items, listener, true, true);
    }

    public AdminProductoAdapter(List<Producto> items, OnAdminAction listener,
                                boolean mostrarEditar, boolean mostrarEliminar) {
        this.items = items;
        this.listener = listener;
        this.mostrarEditar = mostrarEditar;
        this.mostrarEliminar = mostrarEliminar;
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
                    .inflate(R.layout.item_admin_producto, parent, false);
        }

        Producto p = items.get(position);

        ImageView imagen = fila.findViewById(R.id.imgAdmin);
        TextView emoji = fila.findViewById(R.id.tvAdminEmoji);
        TextView nombre = fila.findViewById(R.id.tvAdminName);
        TextView precio = fila.findViewById(R.id.tvAdminPrice);
        MaterialButton btnEdit = fila.findViewById(R.id.btnEdit);
        MaterialButton btnDelete = fila.findViewById(R.id.btnDelete);

        // Solo las frutas llevan imagen; los materiales usan emoji.
        int imgRes = p.isProduce() ? ImagenesProducto.paraNombre(p.getName()) : 0;
        if (imgRes != 0) {
            imagen.setImageResource(imgRes);
            imagen.setVisibility(View.VISIBLE);
            emoji.setVisibility(View.GONE);
        } else {
            imagen.setVisibility(View.GONE);
            emoji.setVisibility(View.VISIBLE);
            emoji.setText(p.getEmoji());
        }
        nombre.setText(p.getName());
        precio.setText(p.isProduce()
                ? String.format(Locale.getDefault(), "€%.2f/kg", p.getPrice())
                : String.format(Locale.getDefault(), "€%.2f", p.getPrice()));

        btnEdit.setVisibility(mostrarEditar ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(mostrarEliminar ? View.VISIBLE : View.GONE);
        btnEdit.setOnClickListener(v -> listener.onEdit(p));
        btnDelete.setOnClickListener(v -> listener.onDelete(p));
        return fila;
    }
}
