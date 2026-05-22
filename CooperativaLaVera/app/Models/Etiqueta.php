<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Etiqueta extends Model
{
    protected $table = 'etiquetas';

    protected $fillable = [
        'idUsuario',
        'idFinca',
        'localidad',
        'direccion',
        'higo_fresco',
        'higo_seco',
        'arandano',
        'cereza',
        'ciruela',
    ];
}
