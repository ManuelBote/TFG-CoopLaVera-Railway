<?php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PedidoModel extends Model
{
    protected $table = 'pedido_material';
    public $timestamps = false;

    protected $fillable = ['id_usuario', 'id_material', 'cantidad', 'aprobado'];

    public static function pedidosDeUsuario($idUsuario)
    {
        return self::with(['material'])
            ->where('id_usuario', $idUsuario)
            ->get();
    }

    public static function todosLosPedidos()
    {
        return self::with(['usuario', 'material'])->get();
    }

    public function usuario()
    {
        return $this->belongsTo(Usuario::class, 'id_usuario');
    }

    public function material()
    {
        return $this->belongsTo(MaterialModel::class, 'id_material');
    }
}