# TFG Cooperativa App (Android)

App Android nativa en Java para la Cooperativa La Vera. Es la parte móvil del TFG; convive con una web (carpeta `Frontend/`) y un backend en Laravel (`CooperativaLaVera/`).

La idea es que cualquiera pueda abrir la app sin cuenta y ver el "foro" (noticias, ubicación), y que los socios — una vez logueados — puedan hacer pedidos de material a la cooperativa y registrar entregas de fruta. Los admins, además, gestionan el catálogo.

## Cómo arrancarla

1. Abrir la carpeta `TFG_Cooperativa-APP` desde Android Studio (Hedgehog o más nuevo).
2. Esperar al sync de Gradle. Si pide instalar SDK 34 o build tools, aceptar.
3. Conectar un dispositivo o levantar un emulador con API 26+ (`minSdk = 26`).
4. Run.

No hace falta backend para probarla: por ahora todo va contra datos mock en memoria. Cerrar la app reinicia el catálogo y el historial.

## Credenciales de prueba

Mientras no haya backend, el login es simulado:

- **Admin**: `admin@coop.com` + cualquier contraseña no vacía.
- **Usuario normal**: cualquier otro email + cualquier contraseña.

El registro también es ficticio: valida campos y vuelve al login con un toast.

## Estructura del código

Paquete base: `com.example.tfg_cooperativa`.

```
activities/      MainActivity (única; aloja el NavHostFragment)
fragments/       Una por pantalla (Home, Login, Register, Materials,
                 Products, ProductDetail, Cart, Checkout, Portal, Admin)
adapters/        RecyclerView.Adapter de noticias, productos, carrito,
                 pedidos, items admin y resumen de checkout
models/          Product, CartItem, Order/OrderLine, NewsPost
data/            Singletons en memoria: SampleData (datos de ejemplo),
                 ProductRepository (CRUD para admin), CartManager,
                 OrderHistory
session/         SessionManager (SharedPreferences con datos del usuario
                 y flag admin)
```

Y en `res/`:

```
layout/          activity_main, fragment_*, item_*, nav_header_drawer,
                 dialog_edit_product
menu/            menu_drawer.xml (un único menú con visibilidad
                 dinámica según el rol)
navigation/      nav_graph.xml con todos los destinos y acciones
drawable/        bg_*, ic_nav_*, ic_logo_leaf (provisional),
                 ic_menu_hamburger…
values/          colors, dimens, strings, styles, themes
```

## Navegación

La pantalla principal es `MainActivity`, que monta:

- Un `Toolbar` arriba con el logo (clic → vuelve a Home) y la hamburguesa a la derecha.
- Un `DrawerLayout` con `NavigationView` lateral (lado derecho) que carga `menu_drawer.xml`.
- Un `NavHostFragment` con `nav_graph.xml` que define todos los destinos.

El menú lateral cambia según el estado de la sesión, controlado en `MainActivity.refreshMenuVisibility()`:

- **Sin login**: solo "Inicio" e "Iniciar sesión".
- **Usuario logueado**: Inicio, Mi Portal, Realizar Pedido, Entregar Material, Carrito, Cerrar sesión.
- **Admin**: lo anterior + Administración.

## Flujos principales

**Realizar pedido (material)**

Realizar Pedido → lista de materiales → tap en uno → detalle con +/– cantidad → "Añadir al carrito" → Carrito → "Proceder al pago" → Checkout (autorrellena nombre, teléfono, DNI desde la sesión) → "Confirmar pedido". Se crea un `Order` de tipo `PEDIDO`, se vacía el carrito y aparece en Mi Portal → Historial.

**Entregar producto (fruta)**

Entregar Material → lista de productos → tap en uno → detalle con +/– (kg) → formulario de entrega autorrellenado → "Registrar Entrega". Se crea un `Order` de tipo `ENTREGA` y aparece en el historial.

**Admin**

Administración → tres pestañas: Productos, Pedidos, Entregas.
En Productos se puede crear/editar/borrar elementos del catálogo a través de un diálogo (`dialog_edit_product.xml`). Los cambios se ven inmediatamente en los listados de la parte de usuario porque ambos consumen `ProductRepository`.

## Datos mock

Todo el contenido inicial está en [data/SampleData.java](app/src/main/java/com/example/tfg_cooperativa/data/SampleData.java):

- 3 entradas de blog para la home.
- 6 materiales con sus precios.
- 6 productos (frutas) con su precio/kg.

Al arrancar la app, `ProductRepository` se inicializa copiando esas listas a estructuras mutables. Lo que el admin añade/edita/borra vive en `ProductRepository` durante la ejecución; se pierde al cerrar la app.

`CartManager` y `OrderHistory` son también singletons en memoria con la misma característica.

## Sesión

`SessionManager` (singleton, respaldado por `SharedPreferences`) guarda:

- `isLoggedIn`, `isAdmin`
- `userId`, `name`, `email`, `phone`, `dni`
- `address`, `city`, `postalCode` (se rellenan al hacer el primer checkout)

Al cerrar sesión se hace `prefs.edit().clear()`. Por eso al arrancar la app después de un logout vuelves al estado "invitado" tal cual.

## Logo

Está pendiente integrar el logo definitivo. Tienes el SVG original en `raw_assets/ICONO.svg`. Para usarlo en la app:

1. En Android Studio, click derecho sobre `app/res/drawable` → **New → Vector Asset**.
2. Selecciona **Local file (SVG, PSD)** y apunta a `TFG_Cooperativa-APP/raw_assets/ICONO.svg`.
3. Nómbralo `ic_logo` y acepta. Android Studio simplifica los paths automáticamente.
4. Sustituye `@drawable/ic_logo_leaf` por `@drawable/ic_logo` en:
   - [activity_main.xml](app/src/main/res/layout/activity_main.xml) (cabecera principal)
   - [nav_header_drawer.xml](app/src/main/res/layout/nav_header_drawer.xml) (cabecera del drawer)

Para el icono del launcher (el que sale en el cajón de apps): click derecho sobre `res` → **New → Image Asset** → **Launcher Icons (Adaptive and Legacy)** → como Source Asset elige el mismo SVG. Genera todas las densidades.

Si Vector Asset se queja del SVG por ser muy complejo, lo más rápido es exportarlo a PNG de 512×512 fuera de Android Studio (Inkscape, Figma, GIMP) y usar el PNG como Source en Image Asset.

## Qué falta (roadmap)

- Conectar al backend Laravel (sustituir `SampleData`/`ProductRepository`/`SessionManager.login` por llamadas reales).
- Cliente HTTP (Retrofit u OkHttp + Gson) y manejo de token Sanctum.
- Persistir el carrito y el historial entre arranques (Room o SharedPreferences serializado).
- Pantalla de confirmación de pedido y de detalle de un pedido pasado.
- Imágenes reales de productos (los emojis son provisionales para tener algo visible mientras se decide qué hacer con la galería).
- Adaptar al diseño definitivo del mockup `Foro.png` cuando esté validado.

## Convenciones

- Java 8. Sin Kotlin de momento porque el TFG arrancó con Java y mezclar añade fricción.
- Navigation Component con un único `MainActivity`.
- Material Components 1.13. No usar `androidx.appcompat.widget.AppCompatButton` por defecto: si tocas un botón, hereda del estilo `Widget.CooperativaLaVera.Button` para mantener coherencia.
- Strings siempre en `res/values/strings.xml`. Nada hardcodeado en los layouts ni en el código (excepto formatos numéricos con `Locale.getDefault()`).
- Los textos del UI están en español. Los identificadores y comentarios, en inglés/español indistintamente — lo importante es que el código sea legible.
