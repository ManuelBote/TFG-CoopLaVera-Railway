package com.example.tfg_cooperativa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.data.DatosEjemplo;
import com.example.tfg_cooperativa.models.EntradaForo;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ActividadInicio extends ActividadBase {

    private static final String URL_MAPS = "https://maps.app.goo.gl/Cs1kVbmXPB4ceC4B8";

    private LinearLayout contenedorForo;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_inicio;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuInicio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepararCarruselForo();
        prepararTarjetaUbicacion();
        prepararFilaProductos();
    }


    private void prepararCarruselForo() {
        contenedorForo = findViewById(R.id.contenedorForo);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int anchoTarjeta = (int) (dm.widthPixels * 0.85f);

        List<EntradaForo> entradas = DatosEjemplo.entradasForo();
        LayoutInflater inflador = getLayoutInflater();
        for (EntradaForo entrada : entradas) {
            View tarjeta = inflador.inflate(R.layout.item_foro, contenedorForo, false);
            ponerAncho(tarjeta, anchoTarjeta);

            ImageView imagen = tarjeta.findViewById(R.id.imgForo);
            TextView emoji = tarjeta.findViewById(R.id.tvForoEmoji);
            TextView titulo = tarjeta.findViewById(R.id.tvForoTitulo);
            TextView fecha = tarjeta.findViewById(R.id.tvForoFecha);
            TextView contenido = tarjeta.findViewById(R.id.tvForoContenido);

            if (entrada.tieneImagen()) {
                imagen.setImageResource(entrada.getImagenResId());
                imagen.setVisibility(View.VISIBLE);
                emoji.setVisibility(View.GONE);
            } else {
                imagen.setVisibility(View.GONE);
                emoji.setVisibility(View.VISIBLE);
                emoji.setText(entrada.getEmoji());
            }
            titulo.setText(entrada.getTitulo());
            fecha.setText(entrada.getFecha());
            contenido.setText(entrada.getContenido());

            contenedorForo.addView(tarjeta);
        }
    }

    private void ponerAncho(View v, int ancho) {
        android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            lp = new LinearLayout.LayoutParams(ancho, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            lp.width = ancho;
        }
        v.setLayoutParams(lp);
    }


    private void prepararTarjetaUbicacion() {
        MaterialCardView tarjeta = findViewById(R.id.cardMapa);
        tarjeta.setOnClickListener(v -> abrirMaps());
    }

    private void abrirMaps() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_MAPS));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception ignored) {
            // Sin navegador: lo ignoramos.
        }
    }


    private void prepararFilaProductos() {
        ponerProducto(R.id.prodHigo, R.drawable.prod_higo, R.string.inicio_producto_higo);
        ponerProducto(R.id.prodHigoSeco, R.drawable.prod_higo_seco, R.string.inicio_producto_higo_seco);
        ponerProducto(R.id.prodCereza, R.drawable.prod_cereza, R.string.inicio_producto_cereza);
        ponerProducto(R.id.prodArandano, R.drawable.prod_arandano, R.string.inicio_producto_arandano);
        ponerProducto(R.id.prodCiruela, R.drawable.prod_ciruela, R.string.inicio_producto_ciruela);
    }

    private void ponerProducto(int includeId, int imagenRes, int nombreRes) {
        View tarjeta = findViewById(includeId);
        if (tarjeta == null) return;
        ImageView img = tarjeta.findViewById(R.id.imgInicioProducto);
        TextView nombre = tarjeta.findViewById(R.id.tvInicioProductoNombre);
        if (img != null) img.setImageDrawable(ContextCompat.getDrawable(this, imagenRes));
        if (nombre != null) nombre.setText(nombreRes);
    }
}
