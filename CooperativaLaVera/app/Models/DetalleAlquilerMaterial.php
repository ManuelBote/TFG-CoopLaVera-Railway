<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DetalleAlquilerMaterial extends Model
{
    protected $table = 'detalles_alquiler_material';
    protected $fillable = ['id_solicitud', 'id_material', 'cantidad'];
    public $timestamps = false;

    public function material()
    {
        return $this->belongsTo(Material::class, 'id_material');
    }
}