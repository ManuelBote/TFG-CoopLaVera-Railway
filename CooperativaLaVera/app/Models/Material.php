<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Material extends Model
{
    protected $table = 'materiales';
    protected $fillable = ['nombre', 'descripcion', 'precio', 'unidades', 'imagen_url', 'stock'];
}