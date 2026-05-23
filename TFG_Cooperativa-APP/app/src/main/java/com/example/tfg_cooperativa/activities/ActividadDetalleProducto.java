package com.example.tfg_cooperativa.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.EntregasApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.data.RepositorioProductos;
import com.example.tfg_cooperativa.models.Producto;
import com.example.tfg_cooperativa.session.GestorSesion;
import com.example.tfg_cooperativa.util.ImagenesProducto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class ActividadDetalleProducto extends ActividadBase {

    private Producto producto;

    private TextInputEditText etFecha, etCantCongelados, etCantM, etCantL, etCantJumbo;
    private TextView tvTotalEntrega;
    private MaterialButton btnRegistrarEntrega;
    private final Calendar fechaElegida = Calendar.getInstance();
    private boolean fechaPuesta = false;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_detalle_producto;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = getIntent().getIntExtra(ActividadProductos.EXTRA_ID, -1);
        String tipo = getIntent().getStringExtra(ActividadProductos.EXTRA_TIPO);
        if (tipo == null) tipo = "PRODUCE";

        // Materiales y productos comparten rango de ids: buscamos según el tipo.
        producto = "MATERIAL".equals(tipo)
                ? RepositorioProductos.get().findMaterial(id)
                : RepositorioProductos.get().findProduce(id);
        if (producto == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvEmoji = findViewById(R.id.tvDetailEmoji);
        ImageView imgDetalle = findViewById(R.id.imgDetail);
        TextView tvNombre = findViewById(R.id.tvDetailName);
        TextView tvDescripcion = findViewById(R.id.tvDetailDescription);
        TextView tvPrecio = findViewById(R.id.tvDetailPrice);

        int imgRes = ImagenesProducto.paraNombre(producto.getName());
        if (imgRes != 0) {
            imgDetalle.setImageResource(imgRes);
            imgDetalle.setVisibility(ImageView.VISIBLE);
            tvEmoji.setVisibility(TextView.GONE);
        } else {
            imgDetalle.setVisibility(ImageView.GONE);
            tvEmoji.setVisibility(TextView.VISIBLE);
            tvEmoji.setText(producto.getEmoji());
        }
        tvNombre.setText(producto.getName());
        tvDescripcion.setText(producto.getDescription());
        tvPrecio.setText(String.format(Locale.getDefault(), "Desde €%.2f/kg", precioMasBajo()));

        MaterialCardView cardPrecios = findViewById(R.id.cardPrices);
        MaterialCardView cardEntrega = findViewById(R.id.cardDelivery);
        cardPrecios.setVisibility(MaterialCardView.VISIBLE);
        cardEntrega.setVisibility(MaterialCardView.VISIBLE);

        prepararPrecios();
        prepararFormularioEntrega();
    }


    private void prepararPrecios() {
        ponerPrecio(R.id.tvPriceCongelado, R.string.detail_price_format_congelado, producto.getPriceCongelado());
        ponerPrecio(R.id.tvPriceM, R.string.detail_price_format_m, producto.getPriceM());
        ponerPrecio(R.id.tvPriceL, R.string.detail_price_format_l, producto.getPriceL());
        ponerPrecio(R.id.tvPriceJumbo, R.string.detail_price_format_jumbo, producto.getPriceJumbo());
    }

    private void ponerPrecio(int textId, int formatoRes, Double valor) {
        TextView tv = findViewById(textId);
        if (valor == null) {
            tv.setVisibility(TextView.GONE);
        } else {
            tv.setText(getString(formatoRes, valor));
        }
    }


    private void prepararFormularioEntrega() {
        etFecha = findViewById(R.id.etDeliveryDate);
        etCantCongelados = findViewById(R.id.etCantCongelados);
        etCantM = findViewById(R.id.etCantM);
        etCantL = findViewById(R.id.etCantL);
        etCantJumbo = findViewById(R.id.etCantJumbo);
        tvTotalEntrega = findViewById(R.id.tvDeliveryTotal);
        btnRegistrarEntrega = findViewById(R.id.btnRegisterDelivery);

        etFecha.setOnClickListener(v -> mostrarSelectorFecha());

        TextWatcher vigilante = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { actualizarTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etCantCongelados.addTextChangedListener(vigilante);
        etCantM.addTextChangedListener(vigilante);
        etCantL.addTextChangedListener(vigilante);
        etCantJumbo.addTextChangedListener(vigilante);

        btnRegistrarEntrega.setOnClickListener(v -> registrarEntrega());
        actualizarTotal();
    }

    private void mostrarSelectorFecha() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (picker, anio, mes, dia) -> {
            fechaElegida.set(Calendar.YEAR, anio);
            fechaElegida.set(Calendar.MONTH, mes);
            fechaElegida.set(Calendar.DAY_OF_MONTH, dia);
            fechaPuesta = true;
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
            etFecha.setText(formato.format(fechaElegida.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarTotal() {
        int cCong = enteroDe(etCantCongelados);
        int cM = enteroDe(etCantM);
        int cL = enteroDe(etCantL);
        int cJ = enteroDe(etCantJumbo);
        double total = cCong * sinNull(producto.getPriceCongelado())
                + cM * sinNull(producto.getPriceM())
                + cL * sinNull(producto.getPriceL())
                + cJ * sinNull(producto.getPriceJumbo());
        tvTotalEntrega.setText(String.format(Locale.getDefault(), "€%.2f", total));
    }

    private void registrarEntrega() {
        int cCong = enteroDe(etCantCongelados);
        int cM = enteroDe(etCantM);
        int cL = enteroDe(etCantL);
        int cJ = enteroDe(etCantJumbo);

        if (cCong + cM + cL + cJ <= 0) {
            Toast.makeText(this, R.string.detail_delivery_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fechaPuesta) {
            Toast.makeText(this, R.string.detail_delivery_no_date, Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String fecha = iso.format(fechaElegida.getTime());
        int idUsuario = GestorSesion.get(this).getUserId();

        btnRegistrarEntrega.setEnabled(false);
        btnRegistrarEntrega.setText(R.string.loading);

        EntregasApi.crearEntrega(this, idUsuario, producto.getId(), fecha, cCong, cM, cL, cJ,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        if (estaInactiva()) return;
                        Toast.makeText(ActividadDetalleProducto.this,
                                R.string.toast_delivery_registered, Toast.LENGTH_SHORT).show();
                        abrir(ActividadPortal.class);
                    }
                    @Override
                    public void onError(String message) {
                        if (estaInactiva()) return;
                        btnRegistrarEntrega.setEnabled(true);
                        btnRegistrarEntrega.setText(R.string.detail_register_delivery);
                        // El back rechaza la entrega (p. ej. cupo de etiquetas insuficiente)
                        // con un 422 y un mensaje claro: lo mostramos en un diálogo.
                        new AlertDialog.Builder(ActividadDetalleProducto.this)
                                .setTitle(R.string.detail_delivery_rejected_title)
                                .setMessage(MapeadorErrores.paraGenerico(
                                        ActividadDetalleProducto.this, message))
                                .setPositiveButton(R.string.btn_close, null)
                                .show();
                    }
                });
    }


    private int enteroDe(EditText edit) {
        if (edit.getText() == null || TextUtils.isEmpty(edit.getText().toString())) return 0;
        try {
            return Integer.parseInt(edit.getText().toString().trim());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private double sinNull(Double d) {
        return d == null ? 0d : d;
    }

    private double precioMasBajo() {
        double mejor = Double.MAX_VALUE;
        Double[] precios = {producto.getPriceCongelado(), producto.getPriceM(),
                producto.getPriceL(), producto.getPriceJumbo()};
        for (Double p : precios) {
            if (p != null && p > 0 && p < mejor) mejor = p;
        }
        return mejor == Double.MAX_VALUE ? 0d : mejor;
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
