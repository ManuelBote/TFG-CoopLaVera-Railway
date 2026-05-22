package com.example.tfg_cooperativa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.adapters.ProductoAdapter;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.CatalogoApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.models.Producto;

import java.util.List;

public class ActividadProductos extends ActividadBase {

    public static final String EXTRA_ID = "itemId";
    public static final String EXTRA_TIPO = "itemKind";

    private ListView lista;
    private ProgressBar progreso;
    private TextView tvError;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_productos;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuProductos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lista = findViewById(R.id.rvProducts);
        progreso = findViewById(R.id.progressProducts);
        tvError = findViewById(R.id.tvProductsError);

        cargarProductos();
    }

    private void cargarProductos() {
        progreso.setVisibility(View.VISIBLE);
        lista.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        CatalogoApi.listProductos(this, new ApiCallback<List<Producto>>() {
            @Override
            public void onSuccess(List<Producto> list) {
                if (estaInactiva()) return;
                progreso.setVisibility(View.GONE);
                if (list.isEmpty()) {
                    tvError.setText(R.string.products_empty);
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }
                lista.setAdapter(new ProductoAdapter(list, ActividadProductos.this::abrirDetalle));
                lista.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                if (estaInactiva()) return;
                progreso.setVisibility(View.GONE);
                tvError.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadProductos.this, message)));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void abrirDetalle(Producto producto) {
        Intent intent = new Intent(this, ActividadDetalleProducto.class);
        intent.putExtra(EXTRA_ID, producto.getId());
        intent.putExtra(EXTRA_TIPO, producto.getKind().name());
        startActivity(intent);
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
