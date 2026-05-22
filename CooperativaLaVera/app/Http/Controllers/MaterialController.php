<?php
namespace App\Http\Controllers;

use App\Models\Material;
use Illuminate\Http\Request;


class MaterialController extends Controller
{
    // Usuarios y admin: listar todos los materiales activos
    public function index()
    {
        return Material::all();
    }

    // Usuarios y admin: ver un material específico
    public function show($id)
    {

    }

    // Solo admin: crear material
    public function store(Request $r)
    {

    }

    // Solo admin: actualizar material
    public function update(Request $r, $id)
    {

    }

    // Solo admin: eliminar material
    public function destroy($id)
    {

    }
}
