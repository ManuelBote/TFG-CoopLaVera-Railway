package com.example.tfg_cooperativa.models;

import org.json.JSONObject;

public class Producto {

    public enum Kind { MATERIAL, PRODUCE }

    private final int id;
    private final String name, emoji, description;
    private final double price;
    private final Kind kind;

    // Material
    private final Integer stock;
    private final String unidades;

    // Precio frutas
    private final Double priceCongelado, priceM, priceL, priceJumbo;

    private final String imageUrl;

    public Producto(int id, String name, String emoji, String description, double price, Kind kind) {
        this(id, name, emoji, description, price, kind,
                null, null, null, null, null, null, null);
    }

    public Producto(int id, String name, String emoji, String description, double price, Kind kind,
                    Integer stock, String unidades,
                    Double priceCongelado, Double priceM, Double priceL, Double priceJumbo,
                    String imageUrl) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.price = price;
        this.kind = kind;
        this.stock = stock;
        this.unidades = unidades;
        this.priceCongelado = priceCongelado;
        this.priceM = priceM;
        this.priceL = priceL;
        this.priceJumbo = priceJumbo;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Kind getKind() { return kind; }
    public Integer getStock() { return stock; }
    public String getUnidades() { return unidades; }
    public Double getPriceCongelado() { return priceCongelado; }
    public Double getPriceM() { return priceM; }
    public Double getPriceL() { return priceL; }
    public Double getPriceJumbo() { return priceJumbo; }
    public String getImageUrl() { return imageUrl; }

    public boolean isProduce() { return kind == Kind.PRODUCE; }


    public static Producto fromMaterialJson(JSONObject o) {
        return new Producto(
                o.optInt("id"),
                o.optString("nombre", ""),
                "📦",
                o.optString("descripcion", ""),
                o.optDouble("precio", 0d),
                Kind.MATERIAL,
                o.has("stock") && !o.isNull("stock") ? o.optInt("stock") : null,
                o.optString("unidades", ""),
                null, null, null, null,
                o.optString("imagen_url", null)
        );
    }

    public static Producto fromProductoJson(JSONObject o) {
        double m = o.optDouble("precio_m", 0d);
        return new Producto(
                o.optInt("id"),
                o.optString("nombre", ""),
                "🍎",
                o.optString("descripcion", ""),
                m,
                Kind.PRODUCE,
                null, null,
                o.has("precio_congelado") && !o.isNull("precio_congelado") ? o.optDouble("precio_congelado") : null,
                o.has("precio_m") && !o.isNull("precio_m") ? o.optDouble("precio_m") : null,
                o.has("precio_l") && !o.isNull("precio_l") ? o.optDouble("precio_l") : null,
                o.has("precio_jumbo") && !o.isNull("precio_jumbo") ? o.optDouble("precio_jumbo") : null,
                o.optString("imagen_url", null)
        );
    }
}
