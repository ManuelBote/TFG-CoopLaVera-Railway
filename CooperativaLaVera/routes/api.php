<?php

//Organizar esto

use App\Http\Controllers\EtiquetaController;
use App\Http\Controllers\FincaController;
use App\Http\Controllers\GestionEntregasController;
use App\Http\Controllers\HistorialPreciosController;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\RegistroController;
use App\Http\Controllers\MaterialController;
use App\Http\Controllers\PedidoController;
use App\Http\Controllers\ProductoController;
use App\Http\Controllers\SolicitudAlquilerMaterialController;
use App\Http\Controllers\UsuarioController;
use App\Http\Middleware\EsAdmin;

// Rutas públicas (sin token)
Route::post('/registro', [RegistroController::class, 'registrarUsuario']);
Route::post('/login',    [RegistroController::class, 'loginUsuario']);

// Rutas protegidas (requieren token Sanctum)
Route::middleware('auth:sanctum')->group(function () {

    // === MATERIALES ===
    Route::get('/materiales',       [MaterialController::class, 'index']);
    Route::get('/materiales/{id}',  [MaterialController::class, 'show']);
    Route::get('/mis-materiales', [SolicitudAlquilerMaterialController::class, 'misSolicitudes']);

    // === PRODUCTOS ===
    Route::get('/productos', [ProductoController::class, 'index']);

    // === PEDIDOS ===
    Route::post('/pedidos',         [PedidoController::class, 'store']);
    Route::get('/pedidos/mis',      [PedidoController::class, 'misPedidos']);

    // === USUARIOS (perfil propio) ===
    Route::get('/perfil',           [UsuarioController::class, 'miPerfil']);
    Route::put('/perfil',           [UsuarioController::class, 'updatePerfil']);
    Route::delete('/perfil',        [UsuarioController::class, 'destroyPerfil']);

    // === ENTREGAS (usuario) ===
    Route::get('/entregas',         [GestionEntregasController::class, 'entregasUsuario']);
    Route::post('/gestionEntregas',         [GestionEntregasController::class, 'store']);

    // === FINCAS ===
    Route::get('/fincas', [FincaController::class, 'misFincas']);
    Route::post('/fincas', [FincaController::class, 'store']);
    Route::get('/fincas/{id}', [FincaController::class, 'show']);
    Route::put('/fincas/{id}', [FincaController::class, 'update']);
    Route::delete('/fincas/{id}', [FincaController::class, 'destroy']);

    // === ETIQUETAS ===
    Route::post('/etiquetas', [EtiquetaController::class, 'store']);

    // === Historial de precios ===
    Route::get('/historial-precios', [HistorialPreciosController::class, 'index']);


    // Solo admin
    Route::middleware(EsAdmin::class)->group(function () {
        Route::get('/solicitudes-material', [SolicitudAlquilerMaterialController::class, 'index']);
        Route::post('/solicitudes-material', [SolicitudAlquilerMaterialController::class, 'store']);
        Route::put('/solicitudes-material/{id}', [SolicitudAlquilerMaterialController::class, 'update']);

        Route::get('/materiales',           [MaterialController::class, 'index']);
        Route::post('/materiales',          [MaterialController::class, 'store']);
        Route::put('/materiales/{id}',      [MaterialController::class, 'update']);
        Route::delete('/materiales/{id}',   [MaterialController::class, 'destroy']);

        Route::get('/pedidos',              [PedidoController::class, 'index']);
        Route::get('/pedidos/{id}',         [PedidoController::class, 'show']);
        Route::put('/pedidos/{id}',         [PedidoController::class, 'update']);

        Route::get('/usuarios',             [UsuarioController::class, 'index']);
        Route::put('/usuarios/{id}/baja', [UsuarioController::class, 'darDeBaja']);
        Route::get('/usuarios/{id}', [UsuarioController::class, 'show']);
        Route::put('/usuarios/{id}', [UsuarioController::class, 'update']);

        // Route::put('/usuarios/{id}',        [UsuarioController::class, 'updateAdmin']);
        // Route::delete('/usuarios/{id}',     [UsuarioController::class, 'destroyAdmin']);


        Route::put('/productos/{id}', [ProductoController::class, 'update']);


        // === GESTIÓN ENTREGAS (admin) ===
        Route::get('/gestionEntregas',          [GestionEntregasController::class, 'index']);
        Route::get('/gestionEntregas/{id}',     [GestionEntregasController::class, 'show']);
        Route::put('/gestionEntregas/{id}',     [GestionEntregasController::class, 'update']);
        Route::delete('/gestionEntregas/{id}',  [GestionEntregasController::class, 'destroy']);
        Route::put('/gestionEntregas/{id}/estado', [GestionEntregasController::class, 'cambiarEstado']);
    });
});
