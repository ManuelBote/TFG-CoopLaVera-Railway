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

import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends BaseAdapter {

    public interface OnProductClick {
        void onClick(Producto product);
    }

    private final List<Producto> items;
    private final OnProductClick listener;

    public ProductoAdapter(List<Producto> items, OnProductClick listener) {
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
                    .inflate(R.layout.item_producto, parent, false);
        }

        Producto p = items.get(position);

        ImageView imagen = fila.findViewById(R.id.imgProducto);
        TextView emoji = fila.findViewById(R.id.tvEmoji);
        TextView nombre = fila.findViewById(R.id.tvName);
        TextView descripcion = fila.findViewById(R.id.tvDescription);
        TextView precio = fila.findViewById(R.id.tvPrice);
        TextView accion = fila.findViewById(R.id.tvAction);

        int imgRes = ImagenesProducto.paraNombre(p.getName());
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
        descripcion.setText(p.getDescription());
        precio.setText(String.format(Locale.getDefault(), "€%.2f/kg", p.getPrice()));
        accion.setText(R.string.products_register_delivery);

        fila.setOnClickListener(v -> listener.onClick(p));
        return fila;
    }
}
