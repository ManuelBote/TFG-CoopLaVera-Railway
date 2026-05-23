package com.example.tfg_cooperativa.data;

import com.example.tfg_cooperativa.models.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RepositorioProductos {

    private static RepositorioProductos instance;
    private final List<Producto> materials = new ArrayList<>();
    private final List<Producto> produce = new ArrayList<>();

    private RepositorioProductos() {}

    public static synchronized RepositorioProductos get() {
        if (instance == null) instance = new RepositorioProductos();
        return instance;
    }

    // Cargar back

    public void setMaterials(List<Producto> list) {
        materials.clear();
        if (list != null) materials.addAll(list);
    }

    public void setProduce(List<Producto> list) {
        produce.clear();
        if (list != null) produce.addAll(list);
    }

    // Gets

    public List<Producto> getMaterials() { return Collections.unmodifiableList(materials); }
    public List<Producto> getProduce() { return Collections.unmodifiableList(produce); }

    //Listar todo
    public List<Producto> getAll() {
        List<Producto> all = new ArrayList<>(materials);
        all.addAll(produce);
        return all;
    }

    //Listar por Id
    public Producto findById(int id) {
        for (Producto p : materials) if (p.getId() == id) return p;
        for (Producto p : produce) if (p.getId() == id) return p;
        return null;
    }

    //Listar Productos
    public Producto findProduce(int id) {
        for (Producto p : produce) if (p.getId() == id) return p;
        return null;
    }

    //Listar materiales
    public Producto findMaterial(int id) {
        for (Producto p : materials) if (p.getId() == id) return p;
        return null;
    }


    public Producto addLocal(Producto p) {
        if (p.getKind() == Producto.Kind.MATERIAL) materials.add(p);
        else produce.add(p);
        return p;
    }

    public void replaceLocal(Producto updated) {
        replaceInList(materials, updated);
        replaceInList(produce, updated);
    }

    public void removeLocal(int id) {
        removeInList(materials, id);
        removeInList(produce, id);
    }

    private void replaceInList(List<Producto> list, Producto replacement) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == replacement.getId()) {
                if (list.get(i).getKind() == replacement.getKind()) {
                    list.set(i, replacement);
                } else {
                    list.remove(i);
                }
                return;
            }
        }
    }

    private void removeInList(List<Producto> list, int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) {
                list.remove(i);
                return;
            }
        }
    }
}
