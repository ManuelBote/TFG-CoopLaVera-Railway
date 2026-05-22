<?php
namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class EsAdmin
{
    public function handle(Request $request, Closure $next)
    {
        if ($request->user() && $request->user()->tipo == 2) {
            return $next($request);
        }
        return response(['mensaje' => 'Acceso denegado. Se requiere rol administrador.'], 403);
    }
}