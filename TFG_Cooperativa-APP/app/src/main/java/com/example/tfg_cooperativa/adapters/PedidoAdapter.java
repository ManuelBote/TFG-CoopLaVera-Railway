package com.example.tfg_cooperativa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends BaseAdapter {

    public interface OnPedidoClick {
        void onClick(Pedido pedido);
    }

    private static final SimpleDateFormat FORMATO_FECHA =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

    private final List<Pedido> items;
    private final OnPedidoClick listener;

    public PedidoAdapter(List<Pedido> items) {
        this(items, null);
    }

    public PedidoAdapter(List<Pedido> items, OnPedidoClick listener) {
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
                    .inflate(R.layout.item_pedido, parent, false);
        }

        Pedido order = items.get(position);

        TextView tipo = fila.findViewById(R.id.tvOrderType);
        TextView fecha = fila.findViewById(R.id.tvOrderDate);
        TextView listaItems = fila.findViewById(R.id.tvOrderItems);
        TextView estado = fila.findViewById(R.id.tvOrderStatus);
        TextView total = fila.findViewById(R.id.tvOrderTotal);

        boolean esPedido = order.getType() == Pedido.Type.PEDIDO;
        tipo.setText(esPedido ? R.string.portal_order_type_pedido
                : R.string.portal_order_type_entrega);
        tipo.setBackgroundResource(esPedido ? R.drawable.bg_chip_pedido
                : R.drawable.bg_chip_entrega);
        tipo.setTextColor(ContextCompat.getColor(parent.getContext(),
                esPedido ? R.color.blue_700 : R.color.green_700));

        fecha.setText(order.getDate() != null ? FORMATO_FECHA.format(order.getDate()) : "");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.getLines().size(); i++) {
            if (i > 0) sb.append("\n");
            Pedido.OrderLine linea = order.getLines().get(i);
            sb.append(String.format(Locale.getDefault(), "%s × %d",
                    linea.getName(), linea.getQuantity()));
        }
        listaItems.setText(sb.toString());

        boolean completado = order.getStatus() == Pedido.Status.COMPLETED;
        estado.setText(completado ? R.string.portal_status_completed
                : R.string.portal_status_processing);
        estado.setBackgroundResource(completado ? R.drawable.bg_chip_entrega
                : R.drawable.bg_chip_pedido);
        estado.setTextColor(ContextCompat.getColor(parent.getContext(),
                completado ? R.color.green_700 : R.color.yellow_700));

        total.setText(String.format(Locale.getDefault(), "€%.2f", order.getTotal()));

        if (listener != null) {
            fila.setOnClickListener(v -> listener.onClick(order));
        } else {
            fila.setOnClickListener(null);
        }
        return fila;
    }
}
