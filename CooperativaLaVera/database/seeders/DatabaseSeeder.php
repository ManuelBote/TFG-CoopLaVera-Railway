<?php

namespace Database\Seeders;

use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

/**
 * Seeder principal. En este proyecto la base de datos de producción se carga
 * desde un script SQL externo (la cooperativa ya tiene datos reales: usuarios,
 * materiales, productos, etc.), así que el seeder se deja vacío para que el
 * comando {@code php artisan migrate --seed --force} que ejecuta Railway en
 * cada despliegue no rompa por duplicados o por intentar insertar contra una
 * tabla {@code users} que no usamos (la app autentica contra {@code usuarios}).
 *
 * <p>Si en algún momento se quiere semillar datos demo para un entorno limpio,
 * hazlo aquí con {@code firstOrCreate} para mantenerlo idempotente.</p>
 */
class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    public function run(): void
    {
        // intencionadamente vacío — ver doc-block de la clase.
    }
}
