<?php

namespace App\Http\Controllers;


use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use App\Models\Usuario;
use Exception;

class RegistroController extends Controller
{
    function registrarUsuario(Request $r)
    {
        try {
            $r->validate([
                'nombre'    => 'required',
                'dni'       => 'required|unique:usuarios,dni',
                'email'     => 'required|unique:usuarios,email|email:rfc,dns',
                'password'  => 'required',
                'password2' => 'required|same:password'
            ]);
        } catch (\Throwable $th) {
            if (str_contains($th->getMessage(), 'dni')) {
                return response(['mensaje' => 'Este DNI ya está registrado.'], 422);
            }
            if (str_contains($th->getMessage(), 'email')) {
                return response(['mensaje' => 'Este email ya está registrado.'], 422);
            }
            return response(['mensaje' => 'Debes rellenar todos los campos correctamente.'], 422);
        }

        try {
            $u = new Usuario();
            $u->nombre        = $r->nombre;
            $u->dni           = $r->dni;
            $u->apellidos     = $r->apellidos;
            $u->email         = $r->email;
            $u->telefono      = $r->telefono;
            $u->direccion     = $r->direccion;
            $u->localidad     = $r->localidad;
            $u->password_hash = bcrypt($r->password);

            if ($u->save()) {
                $token = $u->createToken('auth_token')->plainTextToken;
                $tokenData = [
                    'id'       => $u->id,
                    'nombre'   => $u->nombre,
                    'correo'   => $u->email,
                    'tipo'     => $u->tipo,
                    'telefono' => $u->telefono,
                    'estado'   => $u->estado,
                ];
                return response(['token' => $token, 'usuario' => $tokenData], 200);
            }
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }

    function loginUsuario(Request $r)
    {
        try {
            $r->validate([
                'email' => 'required|email:rfc,dns',
                'pass'  => 'required'
            ]);
        } catch (\Throwable $th) {
            return response(['mensaje' => 'Debes rellenar todos los campos.'], 422);
        }

        try {
            if (Auth::attempt(['email' => $r->email, 'password' => $r->pass])) {
                $u = Usuario::find(Auth::user()->id);
                $token = $u->createToken('auth_token')->plainTextToken;
                $tokenData = [
                    'id'       => $u->id,
                    'nombre'   => $u->nombre,
                    'correo'   => $u->email,
                    'tipo'     => $u->tipo,
                    'telefono' => $u->telefono,
                    'estado'   => $u->estado,
                ];
                return response(['token' => $token, 'usuario' => $tokenData], 200);
            } else {
                return response(['mensaje' => 'Email o contraseña incorrectos.'], 401);
            }
        } catch (\Throwable $th) {
            return response(['mensaje' => $th->getMessage()], 500);
        }
    }
}