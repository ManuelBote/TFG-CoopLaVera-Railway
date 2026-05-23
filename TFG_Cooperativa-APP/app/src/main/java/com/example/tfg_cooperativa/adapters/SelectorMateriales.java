package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.Producto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectorMateriales {

    public interface OnCantidadChanged {
        void onChanged(double nuevoTotal);
    }

    private final List<Producto> materiales;
    private final int[] cantidades;
    private final OnCantidadChanged listener;

    public SelectorMateriales(LinearLayout contenedor, List<Producto> materiales,
                              OnCantidadChanged listener) {
        this.materiales = materiales;
        this.cantidades = new int[materiales.size()];
        this.listener = listener;
        construir(contenedor);
    }

    private void construir(LinearLayout contenedor) {
        contenedor.removeAllViews();
        LayoutInflater inflador = LayoutInflater.from(contenedor.getContext());
        for (int i = 0; i < materiales.size(); i++) {
            final int pos = i;
            Producto material = materiales.get(i);
            View fila = inflador.inflate(R.layout.item_material_seleccion, contenedor, false);

            ImageView imagen = fila.findViewById(R.id.imgMaterial);
            TextView emoji = fila.findViewById(R.id.tvMaterialEmoji);
            TextView nombre = fila.findViewById(R.id.tvMaterialNombre);
            TextView precio = fila.findViewById(R.id.tvMaterialPrecio);
            TextView cantidad = fila.findViewById(R.id.tvCantidad);
            MaterialButton btnMenos = fila.findViewById(R.id.btnMenos);
            MaterialButton btnMas = fila.findViewById(R.id.btnMas);

            nombre.setText(material.getName());
            precio.setText(String.format(Locale.getDefault(), "€%.2f", material.getPrice()));
            cantidad.setText("0");
            imagen.setVisibility(View.GONE);
            emoji.setVisibility(View.VISIBLE);
            emoji.setText(material.getEmoji());

            btnMas.setOnClickListener(v -> cambiar(pos, +1, cantidad));
            btnMenos.setOnClickListener(v -> cambiar(pos, -1, cantidad));

            contenedor.addView(fila);
        }
    }

    private void cambiar(int pos, int delta, TextView cantidad) {
        int nueva = Math.max(0, cantidades[pos] + delta);
        cantidades[pos] = nueva;
        cantidad.setText(String.valueOf(nueva));
        if (listener != null) listener.onChanged(getTotal());
    }

    public double getTotal() {
        double total = 0;
        for (int i = 0; i < materiales.size(); i++) {
            total += materiales.get(i).getPrice() * cantidades[i];
        }
        return total;
    }

    public List<int[]> getDetalles() {
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < materiales.size(); i++) {
            if (cantidades[i] > 0) {
                out.add(new int[]{materiales.get(i).getId(), cantidades[i]});
            }
        }
        return out;
    }
}
