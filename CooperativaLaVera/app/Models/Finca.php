<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Finca extends Model
{
    public $timestamps = false;
    
    protected $fillable = [
        'propietario',
        'localidad',
        'direccion',
        'higueras',
        'ciruelos',
        'arandanos',
        'cerezos',
        'imagen',
    ];
}