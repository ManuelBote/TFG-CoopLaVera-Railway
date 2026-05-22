# CooperativaLaVera — Backend Laravel

API REST para la Cooperativa La Vera. Consumida por dos clientes:

- **Frontend web** (`../Frontend/`) — HTML + CSS + JS con `axios`.
- **App Android** (`../TFG_Cooperativa-APP/`) — Java, todavía con datos mock; pendiente de conectar a esta API.

Stack: PHP 8.2, Laravel 12, Sanctum (tokens), MySQL.

## Cómo levantarlo en local

```bash
cd CooperativaLaVera
composer install
cp .env.example .env
php artisan key:generate
# editar .env y poner las credenciales de MySQL
php artisan migrate
php artisan serve
```

Por defecto queda escuchando en `http://127.0.0.1:8000`. La API vive bajo `/api`, p. ej. `http://127.0.0.1:8000/api/login`.

> **Importante**: las tablas del dominio (`usuarios`, `materiales`, `productos`, `entrega_produc`, `solicitudes_alquiler_mater`…) todavía no están como migraciones de Laravel. Se crean con el `script.sql` que está en la carpeta de Diseño. `php artisan migrate` solo crea las tablas internas de Laravel (cache, jobs, personal_access_tokens). Antes de probar la API hay que ejecutar el `script.sql` contra la base de datos.

## Variables de entorno relevantes

```ini
APP_URL=http://127.0.0.1:8000

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=cooperativa
DB_USERNAME=root
DB_PASSWORD=secret
```

## Estructura

```
app/Http/Controllers/    Un controlador por dominio:
                         RegistroController     -> login y registro
                         UsuarioController      -> /perfil + admin de usuarios
                         MaterialController     -> CRUD de materiales
                         ProductoController     -> productos (frutas)
                         PedidoController       -> pedidos de material
                         GestionEntregasController -> entregas de fruta
                         HistorialPreciosController
                         SolicitudAlquilerMaterialController
                         DetalleAlquilerMaterialController

app/Http/Middleware/     EsAdmin     -> tipo == 2 en la tabla usuarios

app/Models/              Usuario, Material, Producto, PedidoModel,
                         EntregaProducto, HistorialPrecios,
                         SolicitudAlquilerMaterial,
                         DetalleAlquilerMaterial

routes/api.php           Rutas REST (públicas + protegidas).
routes/web.php           Solo el / por compatibilidad.
```

## Autenticación

Sanctum con **tokens personales (Bearer)**. Flujo:

1. `POST /api/login` con email y contraseña → respuesta con `token` y `usuario`.
2. En todas las rutas protegidas hay que enviar el header:
   ```
   Authorization: Bearer <token>
   ```
3. Cerrar sesión: borrar el token en cliente (no hay endpoint de logout todavía).

El rol se discrimina por `usuarios.tipo`:
- `0` o `1` — usuario normal.
- `2` — administrador (las rutas con middleware `EsAdmin` requieren este valor).

> **Detalle a corregir**: `loginUsuario()` valida el campo como `pass`, no `password`. Cuando conectes la app o el front asegúrate de enviar el campo con ese nombre, o cambia el controlador para aceptar `password` (recomendado).

## Endpoints

### Públicas

| Método | Ruta            | Descripción                       |
|--------|-----------------|-----------------------------------|
| POST   | `/api/registro` | Alta de usuario.                  |
| POST   | `/api/login`    | Devuelve token Sanctum + usuario. |

### Autenticadas (requieren `Authorization: Bearer <token>`)

| Método | Ruta                              | Quién |
|--------|-----------------------------------|-------|
| GET    | `/api/perfil`                     | usuario |
| PUT    | `/api/perfil`                     | usuario |
| DELETE | `/api/perfil`                     | usuario |
| GET    | `/api/materiales`                 | usuario |
| GET    | `/api/materiales/{id}`            | usuario |
| GET    | `/api/productos`                  | usuario |
| POST   | `/api/pedidos`                    | usuario |
| GET    | `/api/pedidos/mis`                | usuario |
| GET    | `/api/entregas`                   | usuario |
| POST   | `/api/gestionEntregas`            | usuario |
| GET    | `/api/historial-precios`          | usuario |

### Solo admin (`tipo == 2`)

| Método | Ruta                                | Función |
|--------|-------------------------------------|---------|
| POST   | `/api/materiales`                   | crear material |
| PUT    | `/api/materiales/{id}`              | editar |
| DELETE | `/api/materiales/{id}`              | borrar |
| GET    | `/api/pedidos`                      | listar todos |
| GET    | `/api/pedidos/{id}`                 | ver uno |
| PUT    | `/api/pedidos/{id}`                 | actualizar |
| GET    | `/api/usuarios`                     | listar |
| GET    | `/api/usuarios/{id}`                | ver |
| PUT    | `/api/usuarios/{id}`                | editar |
| PUT    | `/api/usuarios/{id}/baja`           | dar de baja |
| PUT    | `/api/productos/{id}`               | editar producto |
| GET    | `/api/gestionEntregas`              | listar entregas |
| GET    | `/api/gestionEntregas/{id}`         | ver una |
| PUT    | `/api/gestionEntregas/{id}`         | confirmar/actualizar |
| DELETE | `/api/gestionEntregas/{id}`         | borrar |
| GET    | `/api/solicitudes-material`         | listar |
| POST   | `/api/solicitudes-material`         | crear |
| PUT    | `/api/solicitudes-material/{id}`    | actualizar |

## Consumir desde el Frontend (HTML + JS)

El front ya usa `axios` y sigue siempre el mismo patrón: una constante `UrlBase` en cada `.js` apuntando al backend. Ejemplo del login (`Frontend/Login/Login.js`):

```js
const UrlBase = "http://127.0.0.1:8000/api/";

async function iniciarSesion(email, pass) {
    try {
        const { data } = await axios.post(UrlBase + "login", { email, pass });
        // Guardar token y usuario en localStorage
        localStorage.setItem("token",   data.token);
        localStorage.setItem("usuario", JSON.stringify(data.usuario));
        // Redirigir según el rol
        if (data.usuario.tipo === 2) {
            window.location.href = "../InicioAdministrador/InicioAdministrador.html";
        } else {
            window.location.href = "../PanelUsuario/PanelUsuario.html";
        }
    } catch (err) {
        alert(err.response?.data?.mensaje ?? "Error al iniciar sesión");
    }
}
```

Para las llamadas autenticadas conviene montar un interceptor una vez y olvidarse:

```js
axios.interceptors.request.use((cfg) => {
    const token = localStorage.getItem("token");
    if (token) cfg.headers.Authorization = `Bearer ${token}`;
    return cfg;
});

// A partir de aquí, cualquier llamada lleva el header automáticamente:
const { data: materiales } = await axios.get(UrlBase + "materiales");
```

Cuando el backend esté en Railway, basta con sustituir la `UrlBase` en cada `.js` por la URL pública. Lo ideal sería sacarlo a un único `config.js` importado en todas las páginas; está pendiente.

## Consumir desde Android (Java)

La app todavía está sobre datos mock. Para conectarla, la opción más sencilla y con menos dependencias es **OkHttp + JSONObject**. Si se quiere algo más cómodo, **Retrofit + Gson** es el estándar de la industria.

### Paso 1 — dependencias y permisos

En `app/build.gradle.kts`:

```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")
// Si vais con Retrofit:
// implementation("com.squareup.retrofit2:retrofit:2.11.0")
// implementation("com.squareup.retrofit2:converter-gson:2.11.0")
```

El permiso `INTERNET` ya está en el `AndroidManifest.xml`. Si el backend va por HTTP (no HTTPS) en local, añadir `android:usesCleartextTraffic="true"` al `<application>` o configurar un network security config.

### Paso 2 — capa de red

Crear `com.example.tfg_cooperativa.api.ApiClient`:

```java
public final class ApiClient {

    // Emulador → localhost del host se llama 10.0.2.2:
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    // En dispositivo físico: la IP de tu PC en la LAN, p. ej. http://192.168.1.42:8000/api/
    // En producción: la URL de Railway.

    private static final OkHttpClient http = new OkHttpClient();

    public static void login(String email, String pass, Callback cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("pass",  pass); // ojo: el back espera 'pass', no 'password'
        } catch (JSONException ignored) {}

        RequestBody rb = RequestBody.create(body.toString(),
                MediaType.parse("application/json"));
        Request req = new Request.Builder()
                .url(BASE_URL + "login")
                .post(rb)
                .build();
        http.newCall(req).enqueue(cb);
    }

    public static void getMateriales(String token, Callback cb) {
        Request req = new Request.Builder()
                .url(BASE_URL + "materiales")
                .header("Authorization", "Bearer " + token)
                .get()
                .build();
        http.newCall(req).enqueue(cb);
    }
}
```

### Paso 3 — usarlo desde un Fragment

```java
ApiClient.login(email, pass, new Callback() {
    @Override public void onFailure(Call call, IOException e) {
        requireActivity().runOnUiThread(() ->
            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show());
    }
    @Override public void onResponse(Call call, Response res) throws IOException {
        String json = res.body().string();
        if (!res.isSuccessful()) { /* mostrar error */ return; }
        try {
            JSONObject obj  = new JSONObject(json);
            String     tok  = obj.getString("token");
            JSONObject user = obj.getJSONObject("usuario");
            SessionManager.get(requireContext()).login(
                user.getInt("id"),
                user.getString("nombre"),
                user.getString("correo"),
                user.optString("telefono", ""),
                "", // dni no viene en el payload de login todavía
                user.getInt("tipo") == 2
            );
            requireActivity().runOnUiThread(() ->
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_login_to_portal));
        } catch (JSONException ignored) {}
    }
});
```

> Cosas a recordar para Android:
>
> - En el emulador, `127.0.0.1` apunta al propio emulador, no al PC. Hay que usar `10.0.2.2` para hablar con `localhost` del host.
> - En dispositivo físico, el móvil tiene que estar en la misma red Wi-Fi y usar la IP local del PC. Para que `artisan serve` acepte conexiones externas: `php artisan serve --host=0.0.0.0 --port=8000`.
> - El campo del login es `pass`, no `password` (mismo "gotcha" que en el front).
> - Las llamadas de red NO se pueden hacer en el hilo principal. OkHttp con `enqueue` ya lo gestiona en un hilo de fondo, pero los toasts y la navegación deben volverse al hilo de UI con `runOnUiThread`.

## Despliegue (Railway)

El backend está pensado para desplegarse en Railway con MySQL como add-on. En el `.env` de producción:

- `APP_ENV=production`
- `APP_DEBUG=false`
- `APP_URL=https://<tu-servicio>.up.railway.app`
- Variables de la BD que inyecta Railway (`DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`).

Pasos antes del primer deploy:
1. Generar `APP_KEY` (vía `php artisan key:generate` o pasándolo como variable de entorno).
2. Ejecutar el `script.sql` contra la BD para crear el esquema (mientras no haya migraciones nativas).
3. Subir la rama, dejar que Railway haga el build y comprobar que `/api/login` responde.

El frontend estático que se sirve también desde Railway apunta al mismo backend. Acuérdate de actualizar `UrlBase` en los `.js` antes de subirlo o de meterlo en una variable de build.

## Cosas pendientes / mejoras

- Pasar el esquema (`script.sql`) a migraciones Laravel para que `php artisan migrate` sea autosuficiente.
- Cambiar el campo `pass` por `password` en `loginUsuario()` y devolver 401 en credenciales incorrectas (ahora devuelve 500).
- Quitar el modelo duplicado `User.php` (sobra; usamos `Usuario.php`).
- Añadir `$fillable` y `$hidden` en `Usuario` para no exponer `password_hash` al serializar.
- Endpoint de logout (`Auth::user()->currentAccessToken()->delete()`).
- Validar contraseña con `min:8` en el registro.
- Configurar CORS para permitir el origen del Frontend cuando esté en otro dominio.
