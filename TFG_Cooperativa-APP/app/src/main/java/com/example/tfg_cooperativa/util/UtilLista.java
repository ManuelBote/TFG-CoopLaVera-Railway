package com.example.tfg_cooperativa.util;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public final class UtilLista {

    private UtilLista() {}

    public static void rellenar(LinearLayout contenedor, BaseAdapter adapter) {
        contenedor.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            View fila = adapter.getView(i, null, contenedor);
            contenedor.addView(fila);
        }
    }
}
