const UrlBase = CONFIG.API_URL;

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});

function comprobarSesion() {
  let token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "../Login/Login.html";
  }
}

const FILAS_POR_PAGINA = 20;
let paginaActual = 1;
let solicitudesCache = [];

let materiales = [];
let listaSeleccionados = [];

function rellenarFechaActual() {
  const ahora = new Date();
  ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
  document.getElementById("fechaSolicitud").value = ahora
    .toISOString()
    .slice(0, 16);
}

async function cargarSolicitudes() {
  try {
    const respuesta = await api.get("/solicitudes-material");
    solicitudesCache = respuesta.data;
    paginaActual = 1;
    renderizarPagina();
  } catch (error) {
    console.error("Error al cargar las solicitudes:", error);
  }
}

function renderizarPagina() {
  const totalPaginas = Math.ceil(solicitudesCache.length / FILAS_POR_PAGINA);
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const solicitudesPagina = solicitudesCache.slice(inicio, fin);

  const tbody = document.querySelector(".tabla tbody");
  tbody.innerHTML = "";

  solicitudesPagina.forEach((solicitud) => {
    tbody.innerHTML += `
      <tr>
        <td>${solicitud.id}</td>
        <td>${solicitud.usuario.id}</td>
        <td>${new Date(solicitud.created_at).toLocaleString("es-ES")}</td>
        <td class="estado-${solicitud.estado}">${solicitud.estado}</td>
        <td>
  <div class="acciones-contenedor">
    <button class="desplegar" onclick="toggleDetalles(${solicitud.id})">Desplegar detalles</button>
    ${
      solicitud.estado === "pendiente"
        ? `
      <button class="aceptar" onclick="cambiarEstado(${solicitud.id}, 'aceptada')">Aceptar</button>
      <button class="rechazar" onclick="cambiarEstado(${solicitud.id}, 'rechazada')">Rechazar</button>
    `
        : ""
    }
  </div>
</td>
      </tr>
      <tr class="fila-detalles oculto" id="detalles-${solicitud.id}">
        <td colspan="5">
          <table class="tabla-detalles">
            <thead>
              <tr>
                <th>Material</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
              </tr>
            </thead>
            <tbody>
              ${solicitud.detalles
                .map(
                  (detalle) => `
                <tr>
                  <td>${detalle.material.nombre}</td>
                  <td>${detalle.cantidad}</td>
                  <td>${detalle.material.precio} €</td>
                </tr>
              `,
                )
                .join("")}
            </tbody>
          </table>
        </td>
      </tr>
    `;
  });

  const paginacion = document.getElementById("paginacion");
  paginacion.innerHTML = "";

  if (paginaActual > 1) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual - 1})">← Anterior</button>`;
  }

  paginacion.innerHTML += `<span> Página ${paginaActual} de ${totalPaginas || 1} </span>`;

  if (paginaActual < totalPaginas) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual + 1})">Siguiente →</button>`;
  }
}

function cambiarPagina(pagina) {
  paginaActual = pagina;
  renderizarPagina();
}

function toggleDetalles(id) {
  const fila = document.getElementById(`detalles-${id}`);
  const btn = fila.previousElementSibling.querySelector(".btn-flecha");
  if (fila.classList.contains("oculto")) {
    fila.classList.remove("oculto");
    btn.textContent = "Ocultar detalles";
  } else {
    fila.classList.add("oculto");
    btn.textContent = "Desplegar detalles";
  }
}

async function cargarMateriales() {
  try {
    const respuesta = await api.get("/materiales");
    materiales = respuesta.data;

    const select = document.getElementById("selectMaterial");
    const tbodyStock = document.querySelector(".tablaStock tbody");

    materiales.forEach((material) => {
      select.innerHTML += `<option value="${material.id}">${material.nombre}</option>`;
      tbodyStock.innerHTML += `
        <tr>
          <td>${material.id}</td>
          <td>${material.nombre}</td>
          <td>${material.stock}</td>
        </tr>
      `;
    });
  } catch (error) {
    console.error("Error al cargar materiales:", error);
  }
}

function anadirMaterial() {
  const idSeleccionado = parseInt(
    document.getElementById("selectMaterial").value,
  );
  const cantidad = parseInt(document.getElementById("cantidadMaterial").value);
  const material = materiales.find((m) => m.id === idSeleccionado);

  if (!material || !cantidad || cantidad < 1) return;

  const yaEnLista = listaSeleccionados.find((m) => m.id === idSeleccionado);
  const cantidadTotal = yaEnLista ? yaEnLista.cantidad + cantidad : cantidad;
  if (cantidadTotal > material.stock) {
    mostrarMensaje(
      `Stock insuficiente para ${material.nombre}. Stock disponible: ${material.stock}`,
    );
    return;
  }

  const existente = listaSeleccionados.find((m) => m.id === idSeleccionado);
  if (existente) {
    existente.cantidad += cantidad;
  } else {
    listaSeleccionados.push({
      id: material.id,
      nombre: material.nombre,
      precio: material.precio,
      cantidad,
    });
  }

  renderizarLista();
}

function renderizarLista() {
  const ul = document.getElementById("listaMateriales");
  ul.innerHTML = "";

  listaSeleccionados.forEach((item, index) => {
    ul.innerHTML += `
      <li>
        ${item.nombre} — Cantidad: ${item.cantidad} — ${item.precio} €/ud
        <button onclick="eliminarMaterial(${index})">✕</button>
      </li>
    `;
  });
}

function eliminarMaterial(index) {
  listaSeleccionados.splice(index, 1);
  renderizarLista();
}

async function enviarSolicitud() {
  const idUsuario = document.getElementById("idUsuario").value;
  const fecha = document.getElementById("fechaSolicitud").value;

  if (!idUsuario || !fecha || listaSeleccionados.length === 0) {
    mostrarMensaje("Rellena todos los campos y añade al menos un material.");
    return;
  }

  const datos = {
    id_usuario: parseInt(idUsuario),
    fecha: fecha,
    detalles: listaSeleccionados.map((m) => ({
      id_material: m.id,
      cantidad: m.cantidad,
    })),
  };

  try {
    await api.post("/solicitudes-material", datos);
    mostrarMensaje("Solicitud enviada correctamente.");
    listaSeleccionados = [];
    renderizarLista();
    await cargarSolicitudes();
  } catch (error) {
    mostrarMensaje("Error al enviar la solicitud.");
  }
}

async function cambiarEstado(id, estado) {
  try {
    await api.put(`/solicitudes-material/${id}`, { estado });
    await cargarSolicitudes();
    document.querySelector(".tablaStock tbody").innerHTML = "";
    document.getElementById("selectMaterial").innerHTML =
      '<option value="">-- Selecciona un material --</option>';
    await cargarMateriales();
  } catch (error) {
    console.error("Error al cambiar el estado:", error);
  }
}

function mostrarMensaje(texto) {
  let mensaje = document.getElementById("mensaje");
  mensaje.innerHTML = texto;
  setTimeout(() => {
    mensaje.textContent = "";
  }, 4000);
}

function filtrarSolicitudes() {
  const socio = document.getElementById("filtroSocio").value;
  const fecha = document.getElementById("filtroFecha").value;

  const solicitudesFiltradas = solicitudesCache.filter((solicitud) => {
    const coincideSocio =
      socio === "" || String(solicitud.usuario.id) === socio;
    const fechaSolicitud = new Date(solicitud.created_at).toLocaleDateString(
      "en-CA",
    );
    const coincideFecha = fecha === "" || fechaSolicitud === fecha;
    return coincideSocio && coincideFecha;
  });

  const totalPaginas = Math.ceil(
    solicitudesFiltradas.length / FILAS_POR_PAGINA,
  );
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const solicitudesPagina = solicitudesFiltradas.slice(inicio, fin);

  const tbody = document.querySelector(".tabla tbody");
  tbody.innerHTML = "";

  solicitudesPagina.forEach((solicitud) => {
    tbody.innerHTML += `
      <tr>
        <td>${solicitud.id}</td>
        <td>${solicitud.usuario.id}</td>
        <td>${new Date(solicitud.created_at).toLocaleString("es-ES")}</td>
        <td class="estado-${solicitud.estado}">${solicitud.estado}</td>
        <td>
          <button class="btn-flecha" onclick="toggleDetalles(${solicitud.id})">Desplegar detalles</button>
          ${
            solicitud.estado === "pendiente"
              ? `
            <button onclick="cambiarEstado(${solicitud.id}, 'aceptada')">Aceptar</button>
            <button onclick="cambiarEstado(${solicitud.id}, 'rechazada')">Rechazar</button>
          `
              : ""
          }
        </td>
      </tr>
      <tr class="fila-detalles oculto" id="detalles-${solicitud.id}">
        <td colspan="5">
          <table class="tabla-detalles">
            <thead>
              <tr>
                <th>Material</th>
                <th>Cantidad</th>
                <th>Precio unitario</th>
              </tr>
            </thead>
            <tbody>
              ${solicitud.detalles
                .map(
                  (detalle) => `
                <tr>
                  <td>${detalle.material.nombre}</td>
                  <td>${detalle.cantidad}</td>
                  <td>${detalle.material.precio} €</td>
                </tr>
              `,
                )
                .join("")}
            </tbody>
          </table>
        </td>
      </tr>
    `;
  });

  const paginacion = document.getElementById("paginacion");
  paginacion.innerHTML = "";

  if (paginaActual > 1) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual - 1})">← Anterior</button>`;
  }

  paginacion.innerHTML += `<span> Página ${paginaActual} de ${totalPaginas || 1} </span>`;

  if (paginaActual < totalPaginas) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual + 1})">Siguiente →</button>`;
  }
}

window.onload = function () {
  cargarMateriales();
  cargarSolicitudes();
  rellenarFechaActual();
  document
    .getElementById("btnAnadir")
    .addEventListener("click", anadirMaterial);
  document
    .getElementById("btnEnviar")
    .addEventListener("click", enviarSolicitud);
};
