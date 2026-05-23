package com.example.tfg_cooperativa.util;

import androidx.annotation.DrawableRes;

import com.example.tfg_cooperativa.R;

import java.text.Normalizer;
import java.util.Locale;

public final class ImagenesProducto {

    private ImagenesProducto() {}

     // Devuelve el recurso drawable para el producto, o 0 si no hay coincidencia.
    @DrawableRes
    public static int paraNombre(String nombre) {
        if (nombre == null) return 0;
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        if (n.contains("higo") && n.contains("seco")) return R.drawable.prod_higo_seco;
        if (n.contains("higo")) return R.drawable.prod_higo;
        if (n.contains("arandano")) return R.drawable.prod_arandano;
        if (n.contains("cereza")) return R.drawable.prod_cereza;
        if (n.contains("ciruela")) return R.drawable.prod_ciruela;
        return 0;
    }
}
