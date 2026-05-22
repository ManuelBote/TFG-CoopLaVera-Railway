package com.example.tfg_cooperativa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.session.GestorSesion;
import com.google.android.material.navigation.NavigationView;

public abstract class ActividadBase extends AppCompatActivity {

    protected GestorSesion sesion;
    private DrawerLayout cajon;
    private NavigationView navegacion;

    @LayoutRes
    protected abstract int getLayoutContenido();

    protected int getItemMenuActual() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_base);

        sesion = GestorSesion.get(this);

        Toolbar barra = findViewById(R.id.barraSuperior);
        setSupportActionBar(barra);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        FrameLayout contenedor = findViewById(R.id.contenedorContenido);
        getLayoutInflater().inflate(getLayoutContenido(), contenedor, true);

        cajon = findViewById(R.id.contenedorLateral);
        navegacion = findViewById(R.id.vistaNavegacion);
        cajon.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);

        findViewById(R.id.cabeceraLogo).setOnClickListener(v -> irAInicio());

        findViewById(R.id.botonMenu).setOnClickListener(v -> {
            actualizarVisibilidadMenu();
            cajon.openDrawer(GravityCompat.END);
        });

        navegacion.setNavigationItemSelectedListener(this::onItemMenuSeleccionado);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarVisibilidadMenu();
        marcarItemActual();
    }

    private boolean onItemMenuSeleccionado(@NonNull MenuItem item) {
        int id = item.getItemId();
        cajon.closeDrawer(GravityCompat.END);

        if (id == R.id.menuCerrarSesion) {
            cerrarSesion();
            return true;
        }

        Class<?> destino = null;
        if (id == R.id.menuInicio) destino = ActividadInicio.class;
        else if (id == R.id.menuLogin) destino = ActividadLogin.class;
        else if (id == R.id.menuPortal) destino = ActividadPortal.class;
        else if (id == R.id.menuFincas) destino = ActividadMisFincas.class;
        else if (id == R.id.menuProductos) destino = ActividadProductos.class;
        else if (id == R.id.menuAdministracion) destino = ActividadAdministracion.class;

        if (destino != null && !destino.equals(getClass())) {
            abrir(destino);
        }
        return true;
    }


    protected void abrir(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void irAInicio() {
        if (this instanceof ActividadInicio) return;
        abrir(ActividadInicio.class);
    }

    private void cerrarSesion() {
        sesion.logout();
        Intent intent = new Intent(this, ActividadInicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void actualizarVisibilidadMenu() {
        Menu menu = navegacion.getMenu();
        boolean logueado = sesion.isLoggedIn();
        boolean admin = sesion.isAdmin();
        menu.setGroupVisible(R.id.grupoSiempre, true);
        menu.setGroupVisible(R.id.grupoInvitado, !logueado);
        menu.setGroupVisible(R.id.grupoUsuario, logueado);
        menu.setGroupVisible(R.id.grupoAdmin, logueado && admin);
        menu.setGroupVisible(R.id.grupoSesion, logueado);
    }

    private void marcarItemActual() {
        Menu menu = navegacion.getMenu();
        int actual = getItemMenuActual();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem it = menu.getItem(i);
            it.setChecked(actual != 0 && it.getItemId() == actual);
        }
    }

    @Override
    public void onBackPressed() {
        if (cajon.isDrawerOpen(GravityCompat.END)) {
            cajon.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
