<?php
namespace App\Models;

use Laravel\Sanctum\HasApiTokens;
use Illuminate\Foundation\Auth\User as Authenticatable;

class Usuario extends Authenticatable
{
    use HasApiTokens;

    protected $table = 'usuarios';

    protected $hidden = ['password_hash'];

    protected $fillable = [
        'nombre', 'apellidos', 'email', 'password_hash',
        'telefono', 'dni', 'direccion', 'localidad', 'tipo', 'activo','estado'
    ];

    public function getAuthPassword()
    {
        return $this->password_hash;
    }
}