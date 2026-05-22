<?php
namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\PedidoModel;
use App\Models\MaterialModel;

class PedidoController extends Controller
{
    // Usuario: crear pedido (usa id del token)
    public function store(Request $r)
    {
        try {
            $r->validate([
                'id_material' => 'required|integer|exists:materiales,id',
                'cantidad'    => 'required|integer|min:1',
            ]);
            $idUsuario = $r->user()->id;
            $material = MaterialModel::buscarPorId($r->id_material);
            if (!$material) {
                return response(['mensaje' => 'Material no disponible'], 422);
            }
            if ($material->stock < $r->cantidad) {
                return response(['mensaje' => 'Stock insuficiente'], 422);
            }
            $pedido = PedidoModel::create([
                'id_usuario'  => $idUsuario,
                'id_material' => $r->id_material,
                'cantidad'    => $r->cantidad,
                'aprobado'    => 0,
            ]);
            return response(['mensaje' => 'Pedido realizado', 'pedido' => $pedido], 201);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Usuario: ver sus propios pedidos (id del token)
    public function misPedidos(Request $r)
    {
        try {
            $idUsuario = $r->user()->id;
            $pedidos = PedidoModel::pedidosDeUsuario($idUsuario);
            return response(['pedidos' => $pedidos], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Admin: ver todos los pedidos
    public function index(Request $r)
    {
        try {
            $pedidos = PedidoModel::todosLosPedidos();
            return response(['pedidos' => $pedidos], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Admin: ver un pedido específico
    public function show($id)
    {
        try {
            $pedido = PedidoModel::with(['usuario','material'])->find($id);
            if (!$pedido) {
                return response(['mensaje' => 'Pedido no encontrado'], 404);
            }
            return response(['pedido' => $pedido], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Admin: modificar un pedido (estado, cantidad, etc.)
    public function update(Request $r, $id)
    {
        try {
            $pedido = PedidoModel::find($id);
            if (!$pedido) {
                return response(['mensaje' => 'Pedido no encontrado'], 404);
            }
            $r->validate([
                'aprobado' => 'sometimes|in:0,1',
                'cantidad' => 'sometimes|integer|min:1',
            ]);
            $pedido->update($r->only(['aprobado','cantidad']));
            return response(['mensaje' => 'Pedido actualizado', 'pedido' => $pedido], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
}