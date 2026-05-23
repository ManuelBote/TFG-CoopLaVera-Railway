<?php

namespace App\Http\Controllers;

use App\Models\Etiqueta;
use App\Models\Finca;
use Illuminate\Http\Request;

class EtiquetaController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
{
    $finca = Finca::find($request->idFinca);
    if (!$finca) {
        return response()->json(['mensaje' => 'Finca no encontrada'], 404);
    }

    $etiqueta = new Etiqueta();
    $etiqueta->idUsuario   = $request->idUsuario;
    $etiqueta->idFinca     = $request->idFinca;
    $etiqueta->localidad   = $finca->localidad;
    $etiqueta->direccion   = $finca->direccion;
    $etiqueta->higo_fresco = $request->higo_fresco ?? 0;
    $etiqueta->higo_seco   = $request->higo_seco ?? 0;
    $etiqueta->arandano    = $request->arandano ?? 0;
    $etiqueta->cereza      = $request->cereza ?? 0;
    $etiqueta->ciruela     = $request->ciruela ?? 0;

    $etiqueta->save();

    return response()->json(['mensaje' => 'Etiqueta guardada correctamente'], 201);
}

    /**
     * Display the specified resource.
     */
    public function show(string $id)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, string $id)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        //
    }
}
