package com.example.tfg_cooperativa.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {

    public enum Type { PEDIDO, ENTREGA }

    public enum Status { COMPLETED, PROCESSING }

    private final long id;
    private final Type type;
    private final Date date;
    private final List<OrderLine> lines;
    private final double total;
    private final Status status;

    public Pedido(long id, Type type, Date date, List<OrderLine> lines, double total, Status status) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.lines = lines == null ? new ArrayList<>() : lines;
        this.total = total;
        this.status = status;
    }

    public long getId() { return id; }
    public Type getType() { return type; }
    public Date getDate() { return date; }
    public List<OrderLine> getLines() { return lines; }
    public double getTotal() { return total; }
    public Status getStatus() { return status; }

    public static class OrderLine {
        private final String name;
        private final int quantity;

        public OrderLine(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
    }
}
