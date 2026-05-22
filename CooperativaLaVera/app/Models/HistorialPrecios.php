<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class HistorialPrecios extends Model
{
    protected $table = 'historial_precios';

    public $timestamps = false;

    protected $fillable = [
        'id_producto',
        'precio_congelado',
        'precio_m',
        'precio_l',
        'precio_jumbo',
        'fecha_cambio',
    ];

    protected $casts = [
        'precio_congelado' => 'decimal:2',
        'precio_m'         => 'decimal:2',
        'precio_l'         => 'decimal:2',
        'precio_jumbo'     => 'decimal:2',
        'fecha_cambio'     => 'datetime',
    ];

    public function producto()
    {
        return $this->belongsTo(Producto::class, 'id_producto');
    }
}