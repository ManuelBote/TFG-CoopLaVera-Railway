<?php

namespace App\Http\Controllers;

use App\Models\HistorialPrecios;
use Illuminate\Http\Request;

class HistorialPreciosController extends Controller
{
    public function index()
    {
        $historial = HistorialPrecios::with('producto')
            ->orderBy('fecha_cambio', 'asc')
            ->get();

        // Agrupar por producto
        $agrupado = [];
        foreach ($historial as $registro) {
            $nombreProducto = $registro->producto->nombre;
            $agrupado[$nombreProducto][] = [
                'fecha'             => $registro->fecha_cambio->format('d/m/Y'),
                'precio_congelado'  => $registro->precio_congelado,
                'precio_m'          => $registro->precio_m,
                'precio_l'          => $registro->precio_l,
                'precio_jumbo'      => $registro->precio_jumbo,
            ];
        }

        return response()->json($agrupado);
    }
}