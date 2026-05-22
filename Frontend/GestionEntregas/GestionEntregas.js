const UrlBase = "http://127.0.0.1:8000/api/";
const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});

const FILAS_POR_PAGINA = 20;
let paginaActual = 1;
let entregasCache = [];

function cargarFecha() {
  const ahora = new Date();
  ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
  document.getElementById("fechaEntrega").value = ahora
    .toISOString()
    .slice(0, 16);
}

function comprobarSesion() {
  let token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "../Login/Login.html";
  }
}

window.onload = function () {
  recuperarProductos();
  cargarEntregas();
  comprobarSesion();
  cargarFecha();
};

async function recuperarProductos() {
  try {
    const respuesta = await api.get("/productos");
    console.log("Productos:", respuesta.data);
    if (respuesta.data != null) {
      let productos = respuesta.data;
      const select = document.getElementById("productos");
      select.innerHTML = '<option value=""></option>';
      productos.forEach((producto) => {
        select.innerHTML += `<option value="${producto.id}">${producto.nombre}</option>`;
      });
    }
  } catch (error) {
    console.error("Error al recuperar los productos:", error);
  }
}

async function cargarEntregas() {
  try {
    const respuesta = await api.get("/gestionEntregas");
    if (respuesta.data != null) {
      entregasCache = respuesta.data;

      entregasCache.sort((a, b) => {
        const orden = { pendiente: 0, aceptado: 1, rechazado: 2 };
        return orden[a.estado] - orden[b.estado];
      });

      paginaActual = 1;
      renderizarPagina();
    }
  } catch (error) {
    console.error("Error al cargar las entregas:", error);
    mostrarMensaje("Error al cargar las entregas.", "error");
  }
}

function renderizarPagina() {
  const totalPaginas = Math.ceil(entregasCache.length / FILAS_POR_PAGINA);
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const entregasPagina = entregasCache.slice(inicio, fin);

  const tbody = document.querySelector("#tablaEntregas tbody");
  tbody.innerHTML = "";
  entregasPagina.forEach((entrega) => {
    tbody.innerHTML += `
      <tr data-id="${entrega.id}">
        <td>${entrega.id}</td>
        <td>${entrega.id_usuario}</td>
        <td>${entrega.id_producto}</td>
        <td>${entrega.fecha_entrega}</td>
        <td>${entrega.cantidad_congelados}</td>
        <td>${entrega.cantidad_m}</td>
        <td>${entrega.cantidad_l}</td>
        <td>${entrega.cantidad_jumbo}</td>
        <td class="estado-${entrega.estado}">${entrega.estado}</td>
        <td>
          ${entrega.estado === "pendiente" ? `
            <button class="aceptar" onclick="editarEntrega(${entrega.id})">Aceptar</button>
            <button class="rechazar" onclick="cambiarEstado(${entrega.id}, 'rechazado')">Rechazar</button>
          ` : entrega.estado === "aceptado" ? `
            <button class="editar" onclick="editarEntrega(${entrega.id})">Editar</button>
            <button class="borrar" onclick="eliminarEntrega(${entrega.id})">Eliminar</button>
          ` : ``}
        </td>
      </tr>
    `;
  });

  const paginacion = document.getElementById("paginacion");
  paginacion.innerHTML = "";

  if (paginaActual > 1) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual - 1})">← Anterior</button>`;
  }

  paginacion.innerHTML += `<span> Página ${paginaActual} de ${totalPaginas} </span>`;

  if (paginaActual < totalPaginas) {
    paginacion.innerHTML += `<button onclick="cambiarPagina(${paginaActual + 1})">Siguiente →</button>`;
  }
}

function cambiarPagina(pagina) {
  paginaActual = pagina;
  renderizarPagina();
}

async function cambiarEstado(id, estado) {
  try {
    await api.put(`/gestionEntregas/${id}/estado`, { estado });
    cargarEntregas();
    const texto = estado === "aceptado" ? "Entrega aceptada." : "Entrega rechazada.";
    mostrarMensaje(texto, estado === "aceptado" ? "exito" : "error");
  } catch (error) {
    if (error.response) {
      mostrarMensaje(error.response.data.mensaje || "Error al cambiar el estado.", "error");
    } else {
      mostrarMensaje("Error: " + error.message, "error");
    }
  }
}

async function agregarEntrega() {
  const congelados = parseInt(document.getElementById("cCongelados").value) || 0;
  const m = parseInt(document.getElementById("cM").value) || 0;
  const l = parseInt(document.getElementById("cL").value) || 0;
  const jumbo = parseInt(document.getElementById("cJumbo").value) || 0;

  if (congelados === 0 && m === 0 && l === 0 && jumbo === 0) {
    mostrarMensaje("Debes introducir al menos una calidad mayor que 0.", "error");
    return;
  }

  let datos = {
    id_usuario: document.getElementById("idSocio").value,
    id_producto: document.getElementById("productos").value,
    fecha_entrega: document.getElementById("fechaEntrega").value,
    cCongelados: congelados,
    cM: m,
    cL: l,
    cJumbo: jumbo,
  };
  try {
    let respuesta = await api.post("/gestionEntregas", datos);
    if (respuesta.data != null) {
      cargarEntregas();
      document.getElementById("idSocio").value = "";
      document.getElementById("productos").selectedIndex = 0;
      cargarFecha();
      document.getElementById("cCongelados").value = "";
      document.getElementById("cM").value = "";
      document.getElementById("cL").value = "";
      document.getElementById("cJumbo").value = "";
      mostrarMensaje("Entrega añadida correctamente.", "exito");
    }
  } catch (error) {
    if (error.response) {
      mostrarMensaje(error.response.data.mensaje, "error");
    } else {
      mostrarMensaje("Error: " + error.message, "error");
    }
  }
}

async function eliminarEntrega(id) {
  try {
    await api.delete("/gestionEntregas/" + id);
    cargarEntregas();
    mostrarMensaje("Entrega eliminada correctamente.", "exito");
  } catch (error) {
    if (error.response) {
      mostrarMensaje(error.response.data.mensaje || "Error al eliminar.", "error");
    } else {
      mostrarMensaje("Error: " + error.message, "error");
    }
  }
}

async function editarEntrega(id) {
  try {
    const respuesta = await api.get("/gestionEntregas/" + id);
    const entrega = respuesta.data;

    const fila = document.querySelector(`tr[data-id="${id}"]`);
    fila.innerHTML = `
      <td>${entrega.id}</td>
      <td><input type="number" value="${entrega.id_usuario}" id="editIdSocio" /></td>
      <td>
        <select id="editProductos">
          ${document.getElementById("productos").innerHTML}
        </select>
      </td>
      <td><input type="datetime-local" value="${entrega.fecha_entrega.slice(0, 16)}" id="editFechaEntrega" /></td>
      <td><input type="number" value="${entrega.cantidad_congelados}" id="editCCongelados" /></td>
      <td><input type="number" value="${entrega.cantidad_m}" id="editCM" /></td>
      <td><input type="number" value="${entrega.cantidad_l}" id="editCL" /></td>
      <td><input type="number" value="${entrega.cantidad_jumbo}" id="editCJumbo" /></td>
      <td class="estado-${entrega.estado}">${entrega.estado}</td>
      <td>
        <button class="guardar" onclick="guardarEdicion(${entrega.id}, '${entrega.estado}')">Guardar</button>
        <button class="cancelar" onclick="cargarEntregas()">Cancelar</button>
      </td>
    `;

    fila.querySelector("#editProductos").value = entrega.id_producto;
  } catch (error) {
    mostrarMensaje("Error al cargar la entrega.", "error");
  }
}

async function guardarEdicion(id, estadoActual) {
  let datos = {
    id_usuario: document.getElementById("editIdSocio").value,
    id_producto: document.getElementById("editProductos").value,
    fecha_entrega: document.getElementById("editFechaEntrega").value,
    cCongelados: document.getElementById("editCCongelados").value,
    cM: document.getElementById("editCM").value,
    cL: document.getElementById("editCL").value,
    cJumbo: document.getElementById("editCJumbo").value,
  };

  try {
    await api.put("/gestionEntregas/" + id, datos);

    if (estadoActual === "pendiente") {
      await api.put(`/gestionEntregas/${id}/estado`, { estado: "aceptado" });
    }

    cargarEntregas();
    mostrarMensaje("Cambios guardados correctamente.", "exito");
  } catch (error) {
    if (error.response) {
      mostrarMensaje(error.response.data.mensaje || "Error al guardar los cambios.", "error");
    } else {
      mostrarMensaje("Error al guardar los cambios.", "error");
    }
  }
}

function cerrarEditor() {
  document.getElementById("editor").style.display = "none";
}

function filtrarEntregas() {
  const socio = document.getElementById("filtroSocio").value;
  const fecha = document.getElementById("filtroFecha").value;

  const entregasFiltradas = entregasCache.filter((entrega) => {
    const coincideSocio = socio === "" || String(entrega.id_usuario) === socio;
    const fechaEntrega = entrega.fecha_entrega.split(" ")[0];
    const coincideFecha = fecha === "" || fechaEntrega === fecha;
    return coincideSocio && coincideFecha;
  });

  const totalPaginas = Math.ceil(entregasFiltradas.length / FILAS_POR_PAGINA);
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const entregasPagina = entregasFiltradas.slice(inicio, fin);

  const tbody = document.querySelector("#tablaEntregas tbody");
  tbody.innerHTML = "";
  entregasPagina.forEach((entrega) => {
    tbody.innerHTML += `
      <tr data-id="${entrega.id}">
        <td>${entrega.id}</td>
        <td>${entrega.id_usuario}</td>
        <td>${entrega.id_producto}</td>
        <td>${entrega.fecha_entrega}</td>
        <td>${entrega.cantidad_congelados}</td>
        <td>${entrega.cantidad_m}</td>
        <td>${entrega.cantidad_l}</td>
        <td>${entrega.cantidad_jumbo}</td>
        <td class="estado-${entrega.estado}">${entrega.estado}</td>
        <td>
          ${entrega.estado === "pendiente" ? `
            <button class="aceptar" onclick="editarEntrega(${entrega.id})">Aceptar</button>
            <button class="rechazar" onclick="cambiarEstado(${entrega.id}, 'rechazado')">Rechazar</button>
          ` : entrega.estado === "aceptado" ? `
            <button class="editar" onclick="editarEntrega(${entrega.id})">Editar</button>
            <button class="borrar" onclick="eliminarEntrega(${entrega.id})">Eliminar</button>
          ` : ``}
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
    paginacion.innerHTML += `<button class="siguiente" onclick="cambiarPagina(${paginaActual + 1})">Siguiente →</button>`;
  }
}

function mostrarMensaje(mensaje, tipo = "error") {
  const el = document.getElementById("errorMensaje");
  el.textContent = mensaje;
  el.className = "errorMensaje " + tipo;
  el.style.display = "block";

  clearTimeout(el._timeout);
  el._timeout = setTimeout(() => {
    el.style.display = "none";
  }, 4000);
}