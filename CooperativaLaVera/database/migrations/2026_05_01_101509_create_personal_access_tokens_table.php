<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Tabla de Sanctum (personal_access_tokens).
 *
 * Migración idempotente: si la tabla ya existe en la BD (caso típico
 * en producción donde se creó manualmente desde un script SQL), la
 * migración se marca como aplicada sin tocar nada. Si no existe, se
 * crea normalmente. Así el deploy funciona tanto en una BD limpia
 * (desarrollo) como sobre la BD ya poblada en Railway.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasTable('personal_access_tokens')) {
            return;
        }
        Schema::create('personal_access_tokens', function (Blueprint $table) {
            $table->id();
            $table->morphs('tokenable');
            $table->text('name');
            $table->string('token', 64)->unique();
            $table->text('abilities')->nullable();
            $table->timestamp('last_used_at')->nullable();
            $table->timestamp('expires_at')->nullable()->index();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('personal_access_tokens');
    }
};
