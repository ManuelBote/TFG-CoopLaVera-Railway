<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class SolicitudAlquilerMaterial extends Model
{
    protected $table = 'solicitudes_alquiler_material';
    protected $fillable = ['id_usuario', 'estado', 'fecha_solicitud'];

    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'id_usuario');
    }

    public function detalles()
    {
        return $this->hasMany(DetalleAlquilerMaterial::class, 'id_solicitud');
    }
}