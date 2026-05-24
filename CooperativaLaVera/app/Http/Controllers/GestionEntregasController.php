<?php

namespace App\Http\Controllers;

use App\Models\EntregaProducto;
use App\Models\Etiqueta;
use App\Models\Producto;
use App\Models\ProductoCosecha;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\ValidationException;

class GestionEntregasController extends Controller
{
    public function index()
    {
        return EntregaProducto::orderBy('fecha_entrega', 'desc')->get();
    }

    public function store(Request $request)
    {
        try {
            $request->validate([
                'id_usuario'    => 'required',
                'id_producto'   => 'required',
                'fecha_entrega' => 'required',
                'cCongelados'   => 'required',
                'cM'            => 'required',
                'cL'            => 'required',
                'cJumbo'        => 'required',
            ]);
        } catch (\Exception $e) {
            return response(['mensaje' => 'Todos los campos deben estar rellenos.'], 422);
        }

        $total = (int)$request->cCongelados + (int)$request->cM +
            (int)$request->cL + (int)$request->cJumbo;

        if ($total === 0) {
            return response(['mensaje' => 'Debes introducir al menos una calidad.'], 422);
        }

        $mapaProductos = [
            1 => 'higo_fresco',
            2 => 'higo_seco',
            3 => 'arandano',
            4 => 'cereza',
            5 => 'ciruela',
        ];

        $idProducto = (int) $request->id_producto;

        if (!isset($mapaProductos[$idProducto])) {
            return response(['mensaje' => 'Producto no válido.'], 422);
        }

        $columna = $mapaProductos[$idProducto];

        $etiquetasDisponibles = Etiqueta::where('idUsuario', $request->id_usuario)
            ->sum($columna);

        $cajasYaEntregadas = EntregaProducto::where('id_usuario', $request->id_usuario)
            ->where('id_producto', $idProducto)
            ->whereIn('estado', ['pendiente', 'aceptado'])
            ->sum('cantidad_total');

        $cajasEntrega = (int)$request->cCongelados + (int)$request->cM +
            (int)$request->cL + (int)$request->cJumbo;

        $etiquetasRestantes = $etiquetasDisponibles - $cajasYaEntregadas;

        if ($cajasEntrega > $etiquetasRestantes) {
            return response([
                'mensaje' => "No puedes entregar $cajasEntrega cajas. Solo te quedan $etiquetasRestantes etiquetas disponibles de ese producto."
            ], 422);
        }

        try {
            $e = new EntregaProducto();
            $e->id_usuario          = $request->id_usuario;
            $e->id_producto         = $request->id_producto;
            $e->fecha_entrega       = $request->fecha_entrega;
            $e->cantidad_congelados = $request->cCongelados;
            $e->cantidad_m          = $request->cM;
            $e->cantidad_l          = $request->cL;
            $e->cantidad_jumbo      = $request->cJumbo;
            $e->estado              = 'pendiente';
            $e->save();
            return response(['mensaje' => 'Entrega agregada correctamente'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function show(string $id)
    {
        try {
            $e = EntregaProducto::findOrFail($id);
            return response()->json($e);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function update(Request $request, string $id)
    {
        try {
            $e = EntregaProducto::findOrFail($id);
            $e->id_usuario          = $request->id_usuario;
            $e->id_producto         = $request->id_producto;
            $e->fecha_entrega       = $request->fecha_entrega;
            $e->cantidad_congelados = $request->cCongelados;
            $e->cantidad_m          = $request->cM;
            $e->cantidad_l          = $request->cL;
            $e->cantidad_jumbo      = $request->cJumbo;
            $e->save();
            return response(['mensaje' => 'Entrega actualizada correctamente'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function cambiarEstado(Request $request, string $id)
    {
        try {
            $request->validate([
                'estado' => 'required|in:pendiente,aceptado,rechazado',
            ]);

            $e = EntregaProducto::findOrFail($id);
            $e->estado = $request->estado;
            $e->save();
            return response(['mensaje' => 'Estado actualizado correctamente'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function destroy(string $id)
    {
        try {
            $e = EntregaProducto::findOrFail($id);
            $e->delete();
            return response(['mensaje' => 'Entrega eliminada correctamente'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function entregasUsuario(Request $r)
    {
        try {
            $u = Auth::user();

            $entregas = EntregaProducto::where('id_usuario', $u->id)
                ->where('entrega_producto.estado', 'aceptado')
                ->join('productos', 'entrega_producto.id_producto', '=', 'productos.id')
                ->select('productos.nombre', 'fecha_entrega', 'cantidad_total', 'entrega_producto.estado')
                ->orderBy('fecha_entrega', 'asc')
                ->get();

            $agrupado = [];
            foreach ($entregas as $entrega) {
                $agrupado[$entrega->nombre][] = [
                    'fecha'    => $entrega->fecha_entrega,
                    'cantidad' => $entrega->cantidad_total,
                    'estado'   => $entrega->estado,
                ];
            }

            return response(['entregas' => $agrupado], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
}