<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Usuario;
use Illuminate\Support\Facades\Hash;

class UsuarioController extends Controller
{
    // Admin: listar todos los usuarios
    public function index()
    {
        return Usuario::where('activo', 1)->get();
    }

    public function darDeBaja($id)
    {
        try {
            $usuario = Usuario::find($id);
            if (!$usuario) {
                return response(['mensaje' => 'Usuario no encontrado'], 404);
            }
            $usuario->activo = 0;
            $usuario->save();
            return response(['mensaje' => 'Usuario dado de baja'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
    public function show($id)
    {
        try {
            $usuario = Usuario::find($id);
            if (!$usuario) {
                return response(['mensaje' => 'Usuario no encontrado'], 404);
            }
            return response($usuario, 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    public function update(Request $r, $id)
    {
        try {
            $usuario = Usuario::find($id);
            if (!$usuario) {
                return response(['mensaje' => 'Usuario no encontrado'], 404);
            }
            $usuario->update($r->only(['nombre', 'apellidos', 'email', 'telefono', 'dni', 'direccion', 'localidad', 'estado']));
            return response(['mensaje' => 'Usuario actualizado', 'usuario' => $usuario], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Admin: cambiar tipo o activo de un usuario
    public function updateAdmin(Request $r, $id)
    {
        // try {
        //     $usuario = Usuario::find($id);
        //     if (!$usuario) {
        //         return response(['mensaje' => 'Usuario no encontrado'], 404);
        //     }
        //     $r->validate([
        //         'tipo'   => 'sometimes|in:1,2',
        //         'activo' => 'sometimes|in:0,1',
        //     ]);
        //     $usuario->update($r->only(['tipo','activo']));
        //     return response(['mensaje' => 'Usuario actualizado', 'usuario' => $usuario], 200);
        // } catch (\Throwable $th) {
        //     return response(['mensaje' => $th->getMessage()], 500);
        // }
    }

    // Admin: eliminar cualquier usuario
    public function destroyAdmin($id)
    {
        // try {
        //     $usuario = Usuario::find($id);
        //     if (!$usuario) {
        //         return response(['mensaje' => 'Usuario no encontrado'], 404);
        //     }
        //     $usuario->delete();
        //     return response(['mensaje' => 'Usuario eliminado'], 200);
        // } catch (\Throwable $th) {
        //     return response(['mensaje' => $th->getMessage()], 500);
        // }
    }

    // Usuario: ver sus propios datos (solo si el id del token coincide)
    public function miPerfil(Request $r)
    {
        try {
            $usuario = Usuario::select(
                'id',
                'nombre',
                'apellidos',
                'email',
                'telefono',
                'dni',
                'direccion',
                'localidad',
                'tipo',
                'activo',
                'estado',
                'created_at'
            )->find($r->user()->id);
            return response(['usuario' => $usuario], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Usuario: modificar sus propios datos (NO puede cambiar tipo)
    public function updatePerfil(Request $r)
    {
        try {
            $usuario = Usuario::find($r->user()->id);
            $r->validate([
                'nombre'    => 'sometimes|string|max:100',
                'apellidos' => 'sometimes|string|max:150',
                'email'     => 'sometimes|email|unique:usuarios,email,' . $usuario->id,
                'telefono'  => 'sometimes|string|max:20',
                'direccion' => 'sometimes|string',
                'localidad' => 'sometimes|string|max:100',
                'password'  => 'sometimes|min:8|confirmed',
            ]);
            $datos = $r->only(['nombre', 'apellidos', 'email', 'telefono', 'dni', 'direccion', 'localidad']);
            if ($r->filled('password')) {
                $datos['password_hash'] = bcrypt($r->password);
            }
            $usuario->update($datos);
            return response(['mensaje' => 'Perfil actualizado', 'usuario' => $usuario], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    // Usuario: eliminar su propia cuenta
    public function destroyPerfil(Request $r)
    {
        try {
            $usuario = Usuario::find($r->user()->id);
            $usuario->tokens()->delete(); // revocar tokens primero
            $usuario->delete();
            return response(['mensaje' => 'Cuenta eliminada'], 200);
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
}
