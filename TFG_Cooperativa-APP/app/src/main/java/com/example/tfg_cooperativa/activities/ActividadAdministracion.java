package com.example.tfg_cooperativa.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.adapters.AdminProductoAdapter;
import com.example.tfg_cooperativa.adapters.EntregaAdminAdapter;
import com.example.tfg_cooperativa.adapters.SelectorMateriales;
import com.example.tfg_cooperativa.adapters.SolicitudAdapter;
import com.example.tfg_cooperativa.adapters.UsuarioAdminAdapter;
import com.example.tfg_cooperativa.api.ApiCallback;
import com.example.tfg_cooperativa.api.CatalogoApi;
import com.example.tfg_cooperativa.api.EntregasApi;
import com.example.tfg_cooperativa.api.MapeadorErrores;
import com.example.tfg_cooperativa.api.SolicitudesApi;
import com.example.tfg_cooperativa.api.UsuariosApi;
import com.example.tfg_cooperativa.data.RepositorioProductos;
import com.example.tfg_cooperativa.models.EntregaAdmin;
import com.example.tfg_cooperativa.models.Producto;
import com.example.tfg_cooperativa.models.SolicitudAlquiler;
import com.example.tfg_cooperativa.models.UsuarioAdmin;
import com.example.tfg_cooperativa.models.UsuarioResumen;
import com.example.tfg_cooperativa.util.UtilLista;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActividadAdministracion extends ActividadBase {

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private View panelProductos, panelSolicitudes, panelEntregas, panelUsuarios;
    private LinearLayout listaFrutas, listaMateriales;
    private ListView listaSolicitudes, listaEntregas, listaUsuarios;
    private TextView tvSolicitudesVacio, tvEntregasVacio, tvUsuariosVacio;
    private ProgressBar progresoProductos, progresoSolicitudes, progresoEntregas, progresoUsuarios;
    private MaterialButton btnTabProductos, btnTabSolicitudes, btnTabEntregas, btnTabUsuarios;

    private int catalogoPendiente = 0;

    @Override
    protected int getLayoutContenido() {
        return R.layout.contenido_administracion;
    }

    @Override
    protected int getItemMenuActual() {
        return R.id.menuAdministracion;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        panelProductos = findViewById(R.id.panelProducts);
        panelSolicitudes = findViewById(R.id.panelSolicitudes);
        panelEntregas = findViewById(R.id.panelDeliveries);
        panelUsuarios = findViewById(R.id.panelUsuarios);

        listaFrutas = findViewById(R.id.rvAdminFrutas);
        listaMateriales = findViewById(R.id.rvAdminMateriales);
        listaSolicitudes = findViewById(R.id.rvAdminSolicitudes);
        listaEntregas = findViewById(R.id.rvAdminDeliveries);
        listaUsuarios = findViewById(R.id.rvAdminUsuarios);

        tvSolicitudesVacio = findViewById(R.id.tvSolicitudesEmpty);
        tvEntregasVacio = findViewById(R.id.tvDeliveriesEmpty);
        tvUsuariosVacio = findViewById(R.id.tvUsuariosEmpty);

        progresoProductos = findViewById(R.id.progressProducts);
        progresoSolicitudes = findViewById(R.id.progressSolicitudes);
        progresoEntregas = findViewById(R.id.progressDeliveries);
        progresoUsuarios = findViewById(R.id.progressUsuarios);

        btnTabProductos = findViewById(R.id.btnTabProducts);
        btnTabSolicitudes = findViewById(R.id.btnTabSolicitudes);
        btnTabEntregas = findViewById(R.id.btnTabDeliveries);
        btnTabUsuarios = findViewById(R.id.btnTabUsuarios);

        findViewById(R.id.btnNuevaSolicitud).setOnClickListener(v -> abrirDialogoNuevaSolicitud());

        btnTabProductos.setOnClickListener(v -> mostrarPestania(0));
        btnTabSolicitudes.setOnClickListener(v -> mostrarPestania(1));
        btnTabEntregas.setOnClickListener(v -> mostrarPestania(2));
        btnTabUsuarios.setOnClickListener(v -> mostrarPestania(3));

        cargarCatalogo();
        mostrarPestania(0);
    }

    private void mostrarPestania(int indice) {
        panelProductos.setVisibility(indice == 0 ? View.VISIBLE : View.GONE);
        panelSolicitudes.setVisibility(indice == 1 ? View.VISIBLE : View.GONE);
        panelEntregas.setVisibility(indice == 2 ? View.VISIBLE : View.GONE);
        panelUsuarios.setVisibility(indice == 3 ? View.VISIBLE : View.GONE);

        estiloPestania(btnTabProductos, indice == 0);
        estiloPestania(btnTabSolicitudes, indice == 1);
        estiloPestania(btnTabEntregas, indice == 2);
        estiloPestania(btnTabUsuarios, indice == 3);

        if (indice == 1) cargarSolicitudes();
        if (indice == 2) cargarEntregas();
        if (indice == 3) cargarUsuarios();
    }

    private void estiloPestania(MaterialButton btn, boolean activa) {
        if (activa) {
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.green_600, getTheme()));
            btn.setTextColor(getResources().getColor(R.color.white, getTheme()));
            btn.setStrokeWidth(0);
        } else {
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.white, getTheme()));
            btn.setTextColor(getResources().getColor(R.color.green_700, getTheme()));
            btn.setStrokeColor(getResources().getColorStateList(R.color.green_600, getTheme()));
            btn.setStrokeWidth(2);
        }
    }

    // catálogo

    private void cargarCatalogo() {
        catalogoPendiente = 2;
        progresoProductos.setVisibility(View.VISIBLE);
        listaFrutas.setVisibility(View.GONE);
        listaMateriales.setVisibility(View.GONE);

        CatalogoApi.listMateriales(this, new ApiCallback<List<Producto>>() {
            @Override public void onSuccess(List<Producto> list) { catalogoParcial(); }
            @Override public void onError(String message) { catalogoParcial(); }
        });
        CatalogoApi.listProductos(this, new ApiCallback<List<Producto>>() {
            @Override public void onSuccess(List<Producto> list) { catalogoParcial(); }
            @Override public void onError(String message) { catalogoParcial(); }
        });
    }

    private void catalogoParcial() {
        if (estaInactiva()) return;
        catalogoPendiente--;
        if (catalogoPendiente > 0) return;
        progresoProductos.setVisibility(View.GONE);
        refrescarProductos();
    }

    private void refrescarProductos() {
        UtilLista.rellenar(listaFrutas, new AdminProductoAdapter(
                RepositorioProductos.get().getProduce(),
                new AdminProductoAdapter.OnAdminAction() {
                    @Override public void onEdit(Producto p) { mostrarDialogoPrecios(p); }
                    @Override public void onDelete(Producto p) { /* no soportado por el back */ }
                }, true, false));
        listaFrutas.setVisibility(View.VISIBLE);

        UtilLista.rellenar(listaMateriales, new AdminProductoAdapter(
                RepositorioProductos.get().getMaterials(),
                new AdminProductoAdapter.OnAdminAction() {
                    @Override public void onEdit(Producto m) { /* solo lectura */ }
                    @Override public void onDelete(Producto m) { /* solo lectura */ }
                }, false, false));
        listaMateriales.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogoPrecios(Producto p) {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(padding, padding, padding, padding);

        TextInputEditText etCong = campoPrecio(contenedor, getString(R.string.admin_field_price_congelado), p.getPriceCongelado());
        TextInputEditText etM = campoPrecio(contenedor, getString(R.string.admin_field_price_m), p.getPriceM());
        TextInputEditText etL = campoPrecio(contenedor, getString(R.string.admin_field_price_l), p.getPriceL());
        TextInputEditText etJumbo = campoPrecio(contenedor, getString(R.string.admin_field_price_jumbo), p.getPriceJumbo());

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.admin_dialog_edit_prices_title, p.getName()))
                .setView(contenedor)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    Double pc = aDouble(etCong);
                    Double pm = aDouble(etM);
                    Double pl = aDouble(etL);
                    Double pj = aDouble(etJumbo);
                    if (pc == null || pm == null || pl == null || pj == null) {
                        Toast.makeText(this, R.string.error_register_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CatalogoApi.updateProducto(this, p.getId(), pc, pm, pl, pj, new ApiCallback<Void>() {
                        @Override public void onSuccess(Void v) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadAdministracion.this, R.string.admin_save_ok,
                                    Toast.LENGTH_SHORT).show();
                            cargarCatalogo();
                        }
                        @Override public void onError(String message) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadAdministracion.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }

    private TextInputEditText campoPrecio(LinearLayout padre, String hint, Double inicial) {
        TextInputLayout layout = new TextInputLayout(this, null,
                R.style.Widget_CooperativaLaVera_TextInputLayout);
        layout.setHint(hint);

        TextInputEditText edit = new TextInputEditText(layout.getContext());
        edit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (inicial != null) {
            edit.setText(String.format(Locale.US, "%.2f", inicial));
        }
        layout.addView(edit);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
        layout.setLayoutParams(lp);
        padre.addView(layout);
        return edit;
    }

    private Double aDouble(TextInputEditText edit) {
        if (edit.getText() == null || TextUtils.isEmpty(edit.getText().toString())) return null;
        try {
            return Double.parseDouble(edit.getText().toString().trim().replace(",", "."));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    // solicitudes

    private void cargarSolicitudes() {
        progresoSolicitudes.setVisibility(View.VISIBLE);
        listaSolicitudes.setVisibility(View.GONE);
        tvSolicitudesVacio.setVisibility(View.GONE);

        if (RepositorioProductos.get().getMaterials().isEmpty()) {
            CatalogoApi.listMateriales(this, new ApiCallback<List<Producto>>() {
                @Override public void onSuccess(List<Producto> list) { obtenerSolicitudes(); }
                @Override public void onError(String message) { obtenerSolicitudes(); }
            });
        } else {
            obtenerSolicitudes();
        }
    }

    private void obtenerSolicitudes() {
        SolicitudesApi.listar(this, new ApiCallback<List<SolicitudAlquiler>>() {
            @Override public void onSuccess(List<SolicitudAlquiler> list) {
                if (estaInactiva()) return;
                progresoSolicitudes.setVisibility(View.GONE);
                listaSolicitudes.setAdapter(new SolicitudAdapter(list, accionesSolicitud()));
                boolean vacio = list.isEmpty();
                tvSolicitudesVacio.setText(R.string.admin_empty_solicitudes);
                tvSolicitudesVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                listaSolicitudes.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                progresoSolicitudes.setVisibility(View.GONE);
                listaSolicitudes.setAdapter(new SolicitudAdapter(new ArrayList<>(), accionesSolicitud()));
                tvSolicitudesVacio.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message)));
                tvSolicitudesVacio.setVisibility(View.VISIBLE);
                listaSolicitudes.setVisibility(View.GONE);
            }
        });
    }

    private SolicitudAdapter.OnSolicitudAction accionesSolicitud() {
        return new SolicitudAdapter.OnSolicitudAction() {
            @Override public void onAceptar(SolicitudAlquiler s) {
                cambiarEstadoSolicitud(s.getId(), SolicitudAlquiler.ACEPTADA);
            }
            @Override public void onRechazar(SolicitudAlquiler s) {
                cambiarEstadoSolicitud(s.getId(), SolicitudAlquiler.RECHAZADA);
            }
            @Override public void onDetalle(SolicitudAlquiler s) {
                mostrarDetalleSolicitud(s);
            }
        };
    }

    private void mostrarDetalleSolicitud(SolicitudAlquiler s) {
        String fecha = s.getFecha() != null
                ? new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).format(s.getFecha())
                : "-";
        int estadoRes = s.esAceptada() ? R.string.solicitud_estado_aceptada
                : s.esRechazada() ? R.string.solicitud_estado_rechazada
                : R.string.solicitud_estado_pendiente;

        StringBuilder sb = new StringBuilder();
        sb.append("Socio: ").append(s.getNombreUsuario())
                .append("\nFecha: ").append(fecha)
                .append("\nEstado: ").append(getString(estadoRes))
                .append("\n\nMateriales:");
        for (SolicitudAlquiler.Linea l : s.getDetalles()) {
            sb.append("\n• ").append(l.getNombreMaterial()).append(" × ").append(l.getCantidad());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.detalle_solicitud_title)
                .setMessage(sb.toString())
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    private void cambiarEstadoSolicitud(int id, String estado) {
        SolicitudesApi.actualizarEstado(this, id, estado, new ApiCallback<Void>() {
            @Override public void onSuccess(Void v) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this, R.string.solicitud_estado_updated,
                        Toast.LENGTH_SHORT).show();
                obtenerSolicitudes();
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //  nueva solicitud

    private void abrirDialogoNuevaSolicitud() {
        if (RepositorioProductos.get().getMaterials().isEmpty()) {
            CatalogoApi.listMateriales(this, new ApiCallback<List<Producto>>() {
                @Override public void onSuccess(List<Producto> list) { cargarUsuariosYMostrar(); }
                @Override public void onError(String message) {
                    if (estaInactiva()) return;
                    Toast.makeText(ActividadAdministracion.this,
                            MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            cargarUsuariosYMostrar();
        }
    }

    private void cargarUsuariosYMostrar() {
        UsuariosApi.listar(this, new ApiCallback<List<UsuarioResumen>>() {
            @Override public void onSuccess(List<UsuarioResumen> usuarios) {
                if (estaInactiva()) return;
                if (usuarios.isEmpty()) {
                    Toast.makeText(ActividadAdministracion.this, R.string.solicitud_users_load_error,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                mostrarDialogoSolicitud(usuarios);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoSolicitud(List<UsuarioResumen> usuarios) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_solicitud, null, false);

        Spinner spinner = content.findViewById(R.id.spinnerUsuarios);
        TextInputEditText etFecha = content.findViewById(R.id.etFechaSolicitud);
        LinearLayout contenedorMateriales = content.findViewById(R.id.contenedorMateriales);
        TextView tvTotal = content.findViewById(R.id.tvTotalSolicitud);

        ArrayAdapter<UsuarioResumen> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, usuarios);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        final Calendar fechaElegida = Calendar.getInstance();
        final boolean[] fechaPuesta = {false};
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (picker, y, m, d) -> {
                fechaElegida.set(Calendar.YEAR, y);
                fechaElegida.set(Calendar.MONTH, m);
                fechaElegida.set(Calendar.DAY_OF_MONTH, d);
                fechaPuesta[0] = true;
                SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
                etFecha.setText(display.format(fechaElegida.getTime()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        List<Producto> materiales = new ArrayList<>(RepositorioProductos.get().getMaterials());
        SelectorMateriales selectorMateriales = new SelectorMateriales(contenedorMateriales, materiales,
                nuevoTotal -> tvTotal.setText(String.format(Locale.getDefault(), "€%.2f", nuevoTotal)));
        tvTotal.setText(String.format(Locale.getDefault(), "€%.2f", 0d));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.solicitud_dialog_title)
                .setView(content)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_create, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            UsuarioResumen socio = (UsuarioResumen) spinner.getSelectedItem();
            if (socio == null) {
                Toast.makeText(this, R.string.solicitud_err_no_user, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!fechaPuesta[0]) {
                Toast.makeText(this, R.string.solicitud_err_no_date, Toast.LENGTH_SHORT).show();
                return;
            }
            List<int[]> detalles = selectorMateriales.getDetalles();
            if (detalles.isEmpty()) {
                Toast.makeText(this, R.string.solicitud_err_no_materials, Toast.LENGTH_SHORT).show();
                return;
            }

            String fechaIso = ISO.format(fechaElegida.getTime());
            SolicitudesApi.crear(this, socio.getId(), fechaIso, detalles, new ApiCallback<Void>() {
                @Override public void onSuccess(Void v) {
                    if (estaInactiva()) return;
                    Toast.makeText(ActividadAdministracion.this, R.string.solicitud_created_ok,
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    obtenerSolicitudes();
                }
                @Override public void onError(String message) {
                    if (estaInactiva()) return;
                    Toast.makeText(ActividadAdministracion.this,
                            MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                            Toast.LENGTH_LONG).show();
                }
            });
        }));

        dialog.show();
    }

    // entregas

    private void cargarEntregas() {
        progresoEntregas.setVisibility(View.VISIBLE);
        listaEntregas.setVisibility(View.GONE);
        tvEntregasVacio.setVisibility(View.GONE);

        if (RepositorioProductos.get().getProduce().isEmpty()) {
            CatalogoApi.listProductos(this, new ApiCallback<List<Producto>>() {
                @Override public void onSuccess(List<Producto> list) { obtenerEntregas(); }
                @Override public void onError(String message) { obtenerEntregas(); }
            });
        } else {
            obtenerEntregas();
        }
    }

    private void obtenerEntregas() {
        EntregasApi.listAdminDetallado(this, new ApiCallback<List<EntregaAdmin>>() {
            @Override public void onSuccess(List<EntregaAdmin> list) {
                if (estaInactiva()) return;
                progresoEntregas.setVisibility(View.GONE);
                listaEntregas.setAdapter(new EntregaAdminAdapter(list, accionesEntrega()));
                boolean vacio = list.isEmpty();
                tvEntregasVacio.setText(R.string.admin_empty_deliveries);
                tvEntregasVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                listaEntregas.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                progresoEntregas.setVisibility(View.GONE);
                listaEntregas.setAdapter(new EntregaAdminAdapter(new ArrayList<>(), accionesEntrega()));
                tvEntregasVacio.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message)));
                tvEntregasVacio.setVisibility(View.VISIBLE);
                listaEntregas.setVisibility(View.GONE);
            }
        });
    }

    private EntregaAdminAdapter.OnEntregaAction accionesEntrega() {
        return new EntregaAdminAdapter.OnEntregaAction() {
            @Override public void onEditar(EntregaAdmin e) { mostrarDialogoEditarEntrega(e); }
            @Override public void onEliminar(EntregaAdmin e) { confirmarEliminarEntrega(e); }
            @Override public void onDetalle(EntregaAdmin e) { mostrarDetalleEntrega(e); }
            @Override public void onAceptar(EntregaAdmin e) {
                cambiarEstadoEntrega(e.getId(), EntregaAdmin.ACEPTADO);
            }
            @Override public void onRechazar(EntregaAdmin e) {
                cambiarEstadoEntrega(e.getId(), EntregaAdmin.RECHAZADO);
            }
        };
    }

    private void cambiarEstadoEntrega(int id, String estado) {
        EntregasApi.cambiarEstadoEntrega(this, id, estado, new ApiCallback<Void>() {
            @Override public void onSuccess(Void v) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        EntregaAdmin.ACEPTADO.equals(estado)
                                ? R.string.entrega_aceptada_ok
                                : R.string.entrega_rechazada_ok,
                        Toast.LENGTH_SHORT).show();
                obtenerEntregas();
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetalleEntrega(EntregaAdmin e) {
        Date d = parsearFecha(e.getFechaEntrega());
        String fecha = d != null
                ? new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).format(d)
                : "-";

        int estadoRes = e.esAceptado() ? R.string.solicitud_estado_aceptada
                : e.esRechazado() ? R.string.solicitud_estado_rechazada
                : R.string.solicitud_estado_pendiente;

        String msg = "Producto: " + e.getNombreProducto()
                + "\nSocio: #" + e.getIdUsuario()
                + "\nFecha: " + fecha
                + "\nEstado: " + getString(estadoRes)
                + "\n\nCongelado: " + e.getCCongelados() + " kg"
                + "\nM: " + e.getCM() + " kg"
                + "\nL: " + e.getCL() + " kg"
                + "\nJumbo: " + e.getCJumbo() + " kg"
                + "\n\nTotal: " + e.getTotal() + " kg";

        new AlertDialog.Builder(this)
                .setTitle(R.string.detalle_entrega_title)
                .setMessage(msg)
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    private void confirmarEliminarEntrega(EntregaAdmin e) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete)
                .setMessage(R.string.entrega_delete_confirm)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, w) ->
                        EntregasApi.eliminarEntrega(this, e.getId(), new ApiCallback<Void>() {
                            @Override public void onSuccess(Void v) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadAdministracion.this, R.string.entrega_deleted_ok,
                                        Toast.LENGTH_SHORT).show();
                                obtenerEntregas();
                            }
                            @Override public void onError(String message) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadAdministracion.this,
                                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                                        Toast.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }

    private void mostrarDialogoEditarEntrega(EntregaAdmin e) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_entrega, null, false);

        TextInputEditText etSocio = content.findViewById(R.id.etEntregaSocio);
        Spinner spinnerProducto = content.findViewById(R.id.spinnerEntregaProducto);
        TextInputEditText etFecha = content.findViewById(R.id.etEntregaFecha);
        TextInputEditText etCong = content.findViewById(R.id.etEntregaCong);
        TextInputEditText etM = content.findViewById(R.id.etEntregaM);
        TextInputEditText etL = content.findViewById(R.id.etEntregaL);
        TextInputEditText etJumbo = content.findViewById(R.id.etEntregaJumbo);

        final List<Producto> productos = new ArrayList<>(RepositorioProductos.get().getProduce());
        List<String> nombres = new ArrayList<>();
        for (Producto p : productos) nombres.add(p.getName());
        ArrayAdapter<String> prodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        prodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(prodAdapter);

        etSocio.setText(String.valueOf(e.getIdUsuario()));
        etCong.setText(String.valueOf(e.getCCongelados()));
        etM.setText(String.valueOf(e.getCM()));
        etL.setText(String.valueOf(e.getCL()));
        etJumbo.setText(String.valueOf(e.getCJumbo()));
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == e.getIdProducto()) {
                spinnerProducto.setSelection(i);
                break;
            }
        }

        final Calendar fechaElegida = Calendar.getInstance();
        final boolean[] fechaPuesta = {false};
        Date fechaActual = parsearFecha(e.getFechaEntrega());
        if (fechaActual != null) {
            fechaElegida.setTime(fechaActual);
            fechaPuesta[0] = true;
            etFecha.setText(new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).format(fechaActual));
        }
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (picker, y, m, d) -> {
                fechaElegida.set(Calendar.YEAR, y);
                fechaElegida.set(Calendar.MONTH, m);
                fechaElegida.set(Calendar.DAY_OF_MONTH, d);
                fechaPuesta[0] = true;
                etFecha.setText(new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"))
                        .format(fechaElegida.getTime()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.entrega_dialog_edit_title)
                .setView(content)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int pos = spinnerProducto.getSelectedItemPosition();
            String socioStr = etSocio.getText() == null ? "" : etSocio.getText().toString().trim();
            if (socioStr.isEmpty() || pos < 0 || pos >= productos.size() || !fechaPuesta[0]) {
                Toast.makeText(this, R.string.entrega_err_required, Toast.LENGTH_SHORT).show();
                return;
            }
            int idSocio;
            try { idSocio = Integer.parseInt(socioStr); }
            catch (NumberFormatException nfe) {
                Toast.makeText(this, R.string.entrega_err_required, Toast.LENGTH_SHORT).show();
                return;
            }
            String fechaIso = ISO.format(fechaElegida.getTime());
            EntregasApi.actualizarEntrega(this, e.getId(), idSocio, productos.get(pos).getId(), fechaIso,
                    enteroCalibre(etCong), enteroCalibre(etM), enteroCalibre(etL), enteroCalibre(etJumbo),
                    new ApiCallback<Void>() {
                        @Override public void onSuccess(Void v) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadAdministracion.this, R.string.entrega_updated_ok,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            obtenerEntregas();
                        }
                        @Override public void onError(String message) {
                            if (estaInactiva()) return;
                            Toast.makeText(ActividadAdministracion.this,
                                    MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }));

        dialog.show();
    }

    private int enteroCalibre(TextInputEditText et) {
        String s = et.getText() == null ? "" : et.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException nfe) { return 0; }
    }

    // ---------------------------------------------------- usuarios

    private void cargarUsuarios() {
        progresoUsuarios.setVisibility(View.VISIBLE);
        listaUsuarios.setVisibility(View.GONE);
        tvUsuariosVacio.setVisibility(View.GONE);

        UsuariosApi.listarDetallado(this, new ApiCallback<List<UsuarioAdmin>>() {
            @Override public void onSuccess(List<UsuarioAdmin> list) {
                if (estaInactiva()) return;
                progresoUsuarios.setVisibility(View.GONE);
                listaUsuarios.setAdapter(new UsuarioAdminAdapter(list, accionesUsuario()));
                boolean vacio = list.isEmpty();
                tvUsuariosVacio.setText(R.string.admin_empty_users);
                tvUsuariosVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                listaUsuarios.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                progresoUsuarios.setVisibility(View.GONE);
                listaUsuarios.setAdapter(new UsuarioAdminAdapter(new ArrayList<>(), accionesUsuario()));
                tvUsuariosVacio.setText(getString(R.string.error_load_format,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message)));
                tvUsuariosVacio.setVisibility(View.VISIBLE);
                listaUsuarios.setVisibility(View.GONE);
            }
        });
    }

    private UsuarioAdminAdapter.OnUsuarioAction accionesUsuario() {
        return new UsuarioAdminAdapter.OnUsuarioAction() {
            @Override public void onAceptar(UsuarioAdmin u) {
                cambiarEstadoUsuario(u.getId(), UsuarioAdmin.ACEPTADO);
            }
            @Override public void onRechazar(UsuarioAdmin u) {
                cambiarEstadoUsuario(u.getId(), UsuarioAdmin.RECHAZADO);
            }
            @Override public void onDetalle(UsuarioAdmin u) {
                mostrarDetalleUsuario(u);
            }
            @Override public void onEliminar(UsuarioAdmin u) {
                confirmarEliminarUsuario(u);
            }
        };
    }

    private void confirmarEliminarUsuario(UsuarioAdmin u) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.usuario_eliminar_title)
                .setMessage(getString(R.string.usuario_eliminar_confirm, u.getNombreCompleto()))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, w) ->
                        UsuariosApi.darDeBaja(this, u.getId(), new ApiCallback<Void>() {
                            @Override public void onSuccess(Void v) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadAdministracion.this, R.string.usuario_eliminado_ok,
                                        Toast.LENGTH_SHORT).show();
                                cargarUsuarios();
                            }
                            @Override public void onError(String message) {
                                if (estaInactiva()) return;
                                Toast.makeText(ActividadAdministracion.this,
                                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                                        Toast.LENGTH_LONG).show();
                            }
                        }))
                .show();
    }

    private void cambiarEstadoUsuario(int id, String estado) {
        UsuariosApi.cambiarEstado(this, id, estado, new ApiCallback<Void>() {
            @Override public void onSuccess(Void v) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        UsuarioAdmin.ACEPTADO.equals(estado)
                                ? R.string.usuario_aceptado_ok
                                : R.string.usuario_rechazado_ok,
                        Toast.LENGTH_SHORT).show();
                cargarUsuarios();
            }
            @Override public void onError(String message) {
                if (estaInactiva()) return;
                Toast.makeText(ActividadAdministracion.this,
                        MapeadorErrores.paraGenerico(ActividadAdministracion.this, message),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetalleUsuario(UsuarioAdmin u) {
        int estadoRes = u.esAceptado() ? R.string.usuario_estado_aceptado
                : u.esRechazado() ? R.string.usuario_estado_rechazado
                : R.string.usuario_estado_pendiente;

        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(u.getNombreCompleto())
                .append("\nEstado: ").append(getString(estadoRes));
        if (!u.getEmail().isEmpty())     sb.append("\n\nEmail: ").append(u.getEmail());
        if (!u.getTelefono().isEmpty())  sb.append("\nTeléfono: ").append(u.getTelefono());
        if (!u.getDni().isEmpty())       sb.append("\nDNI: ").append(u.getDni());
        if (!u.getDireccion().isEmpty()) sb.append("\nDirección: ").append(u.getDireccion());
        if (!u.getLocalidad().isEmpty()) sb.append("\nLocalidad: ").append(u.getLocalidad());

        new AlertDialog.Builder(this)
                .setTitle(R.string.detalle_usuario_title)
                .setMessage(sb.toString())
                .setPositiveButton(R.string.btn_close, null)
                .show();
    }

    // helpers

    private Date parsearFecha(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String cabeza = raw.length() >= 10 ? raw.substring(0, 10) : raw;
        try { return ISO.parse(cabeza); } catch (ParseException pe) { return null; }
    }

    private boolean estaInactiva() {
        return isFinishing() || isDestroyed();
    }
}
