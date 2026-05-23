<?php

namespace App\Http\Controllers;

use App\Models\DetalleAlquilerMaterial;
use App\Models\Material;
use App\Models\SolicitudAlquilerMaterial;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;

class SolicitudAlquilerMaterialController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $solicitudes = SolicitudAlquilerMaterial::with(['usuario', 'detalles.material'])
            ->orderByRaw("FIELD(estado, 'pendiente', 'aceptada', 'rechazada')")
            ->orderBy('created_at', 'desc')
            ->get();
        return response()->json($solicitudes);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        // Comprobar stock de cada material
        foreach ($request->detalles as $detalle) {
            $material = Material::findOrFail($detalle['id_material']);
            if ($material->stock < $detalle['cantidad']) {
                return response()->json([
                    'error' => "Stock insuficiente para {$material->nombre}. Stock disponible: {$material->stock}"
                ], 422);
            }
        }

        // Crear la solicitud principal
        $solicitud = SolicitudAlquilerMaterial::create([
            'id_usuario' => $request->id_usuario,
            'estado' => 'pendiente',
            'fecha_solicitud' => $request->fecha
        ]);

        // Insertar cada detalle
        foreach ($request->detalles as $detalle) {
            DetalleAlquilerMaterial::create([
                'id_solicitud' => $solicitud->id,
                'id_material' => $detalle['id_material'],
                'cantidad' => $detalle['cantidad']
            ]);
        }

        return response()->json($solicitud->load('detalles.material'), 201);
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
        $solicitud = SolicitudAlquilerMaterial::findOrFail($id);
        $solicitud->update(['estado' => $request->estado]);
        return response()->json($solicitud);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        //
    }

    public function misSolicitudes(Request $r)
    {
        try {
            $u = Auth::user();

            $detalles = DB::table('detalles_alquiler_material')
                ->join('solicitudes_alquiler_material', 'detalles_alquiler_material.id_solicitud', '=', 'solicitudes_alquiler_material.id')
                ->join('materiales', 'detalles_alquiler_material.id_material', '=', 'materiales.id')
                ->where('solicitudes_alquiler_material.id_usuario', $u->id)
                ->where('solicitudes_alquiler_material.estado', 'aceptada')
                ->select('materiales.nombre', 'detalles_alquiler_material.cantidad')
                ->get();

            $agrupado = [];
            foreach ($detalles as $detalle) {
                if (!isset($agrupado[$detalle->nombre])) {
                    $agrupado[$detalle->nombre] = 0;
                }
                $agrupado[$detalle->nombre] += $detalle->cantidad;
            }

            return response(['materiales' => $agrupado], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
}
