<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Producto extends Model
{
    protected $table = 'productos';

    protected $fillable = [
        'nombre',
        'descripcion',
        'imagen_url',
        'precio_congelado',
        'precio_m',
        'precio_l',
        'precio_jumbo',
    ];

    protected $casts = [
        'precio_congelado' => 'decimal:2',
        'precio_m'         => 'decimal:2',
        'precio_l'         => 'decimal:2',
        'precio_jumbo'     => 'decimal:2',
        'created_at'       => 'datetime',
        'updated_at'       => 'datetime',
    ];
}