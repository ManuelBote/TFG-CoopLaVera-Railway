<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class EntregaProducto extends Model
{
    protected $table = 'entrega_producto';

    const UPDATED_AT = null;

    protected $fillable = [
        'id_usuario',
        'id_producto',
        'cantidad_congelados',
        'cantidad_m',
        'cantidad_l',
        'cantidad_jumbo',
        'fecha_entrega',
        'estado',
    ];

    protected $casts = [
        'created_at' => 'datetime',
    ];
}