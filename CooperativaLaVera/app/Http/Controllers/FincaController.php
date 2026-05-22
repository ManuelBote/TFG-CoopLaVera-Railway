<?php

namespace App\Http\Controllers;

use App\Models\Finca;
use Illuminate\Http\Request;

class FincaController extends Controller
{
    public function misFincas()
    {
        $fincas = Finca::where('propietario', auth('sanctum')->id())->get();
        return response()->json($fincas);
    }
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
        $finca = new Finca();
        $finca->propietario = auth('sanctum')->id();
        $finca->localidad   = $request->localidad;
        $finca->direccion   = $request->direccion;
        $finca->higueras    = $request->higueras ?? 0;
        $finca->ciruelos    = $request->ciruelos ?? 0;
        $finca->arandanos   = $request->arandanos ?? 0;
        $finca->cerezos     = $request->cerezos ?? 0;

        if ($request->hasFile('imagen')) {
            $finca->imagen = $request->file('imagen')->store('fincas', 'public');
        }

        $finca->save();

        return response()->json(['mensaje' => 'Finca registrada correctamente'], 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(string $id)
    {
        $finca = Finca::find($id);
        if (!$finca) {
            return response()->json(['mensaje' => 'Finca no encontrada'], 404);
        }
        return response()->json($finca);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, string $id)
    {
        $finca = Finca::find($id);
        if (!$finca) {
            return response()->json(['mensaje' => 'Finca no encontrada'], 404);
        }
        $finca->localidad  = $request->localidad;
        $finca->direccion  = $request->direccion;
        $finca->higueras   = $request->higueras;
        $finca->ciruelos   = $request->ciruelos;
        $finca->arandanos  = $request->arandanos;
        $finca->cerezos    = $request->cerezos;
        $finca->save();
        return response()->json(['mensaje' => 'Finca actualizada correctamente'], 200);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        $finca = Finca::find($id);
        if (!$finca) {
            return response()->json(['mensaje' => 'Finca no encontrada'], 404);
        }
        $finca->delete();
        return response()->json(['mensaje' => 'Finca eliminada correctamente'], 200);
    }
}
