const UrlBase = CONFIG.API_URL;

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});

const FILAS_POR_PAGINA = 20;
let paginaActual = 1;
let usuariosCache = [];

function comprobarSesion() {
  let token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "../Login/Login.html";
  }
}

async function cargarUsuarios() {
  try {
    const respuesta = await api.get("/usuarios");
    usuariosCache = respuesta.data;

    usuariosCache.sort((a, b) => {
      const orden = { pendiente: 0, aceptado: 1, rechazado: 2 };
      return orden[a.estado] - orden[b.estado];
    });

    paginaActual = 1;
    renderizarPagina();
  } catch (error) {
    console.error("Error al cargar los usuarios:", error);
  }
}

function renderizarPagina() {
  const totalPaginas = Math.ceil(usuariosCache.length / FILAS_POR_PAGINA);
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const usuariosPagina = usuariosCache.slice(inicio, fin);

  const body = document.getElementById("grid-body");
  body.innerHTML = "";

  usuariosPagina.forEach((usuario) => {
    const fila = document.createElement("div");
    fila.classList.add("grid-fila");
    fila.dataset.id = usuario.id;
    fila.innerHTML = `
      <div title="${usuario.id}">${usuario.id}</div>
      <div title="${usuario.nombre}">${usuario.nombre}</div>
      <div title="${usuario.apellidos}">${usuario.apellidos}</div>
      <div title="${usuario.email}">${usuario.email}</div>
      <div title="${usuario.telefono}">${usuario.telefono}</div>
      <div title="${usuario.dni}">${usuario.dni}</div>
      <div title="${usuario.direccion}">${usuario.direccion}</div>
      <div title="${usuario.localidad}">${usuario.localidad}</div>
      <div class="estado-${usuario.estado}">${usuario.estado}</div>
      <div>
        <div class="acciones">
          ${usuario.estado === 'pendiente' ? `
            <button class="aceptar" onclick="cambiarEstado(${usuario.id}, 'aceptado')">Aceptar</button>
            <button class="rechazar" onclick="cambiarEstado(${usuario.id}, 'rechazado')">Rechazar</button>
          ` : ''}
          ${usuario.estado === 'aceptado' ? `
            <button class="modificar" onclick="modificarUsuario(${usuario.id})">Modificar</button>
            <button class="baja" onclick="darDeBaja(${usuario.id})">Dar de baja</button>
          ` : ''}
        </div>
      </div>
    `;
    body.appendChild(fila);
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

async function cambiarEstado(id, estado) {
  try {
    await api.put(`/usuarios/${id}`, { estado });
    cargarUsuarios();
  } catch (error) {
    console.error("Error al cambiar el estado:", error);
  }
}

async function darDeBaja(id) {
  if (!confirm('¿Seguro que quieres dar de baja a este usuario?')) return;
  try {
    await api.put(`/usuarios/${id}/baja`);
    cargarUsuarios();
  } catch (error) {
    console.error('Error al dar de baja:', error);
  }
}

async function modificarUsuario(id) {
  try {
    const respuesta = await api.get(`/usuarios/${id}`);
    const usuario = respuesta.data;

    const fila = document.querySelector(`.grid-fila[data-id="${id}"]`);
    fila.innerHTML = `
      <div>${usuario.id}</div>
      <div><input type="text" value="${usuario.nombre}" id="edit-nombre" /></div>
      <div><input type="text" value="${usuario.apellidos}" id="edit-apellidos" /></div>
      <div><input type="email" value="${usuario.email}" id="edit-email" /></div>
      <div><input type="text" value="${usuario.telefono}" id="edit-telefono" /></div>
      <div><input type="text" value="${usuario.dni}" id="edit-dni" /></div>
      <div><input type="text" value="${usuario.direccion}" id="edit-direccion" /></div>
      <div><input type="text" value="${usuario.localidad}" id="edit-localidad" /></div>
      <div class="estado-${usuario.estado}">${usuario.estado}</div>
      <div>
        <div class="acciones">
          <button class="guardar" onclick="guardarUsuario(${usuario.id})">Guardar</button>
          <button class="cancelar" onclick="cargarUsuarios()">Cancelar</button>
        </div>
      </div>
    `;
  } catch (error) {
    console.error('Error al cargar el usuario:', error);
  }
}

async function guardarUsuario(id) {
  try {
    await api.put(`/usuarios/${id}`, {
      nombre:    document.getElementById('edit-nombre').value,
      apellidos: document.getElementById('edit-apellidos').value,
      email:     document.getElementById('edit-email').value,
      telefono:  document.getElementById('edit-telefono').value,
      dni:       document.getElementById('edit-dni').value,
      direccion: document.getElementById('edit-direccion').value,
      localidad: document.getElementById('edit-localidad').value,
    });
    cargarUsuarios();
  } catch (error) {
    console.error('Error al guardar usuario:', error);
  }
}

function filtrarUsuarios() {
  const dni = document.getElementById("filtroDni").value.toLowerCase();

  const usuariosFiltrados = usuariosCache.filter((usuario) =>
    usuario.dni?.toLowerCase().includes(dni)
  );

  const totalPaginas = Math.ceil(usuariosFiltrados.length / FILAS_POR_PAGINA);
  const inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
  const fin = inicio + FILAS_POR_PAGINA;
  const usuariosPagina = usuariosFiltrados.slice(inicio, fin);

  const body = document.getElementById("grid-body");
  body.innerHTML = "";

  usuariosPagina.forEach((usuario) => {
    const fila = document.createElement("div");
    fila.classList.add("grid-fila");
    fila.dataset.id = usuario.id;
    fila.innerHTML = `
      <div title="${usuario.id}">${usuario.id}</div>
      <div title="${usuario.nombre}">${usuario.nombre}</div>
      <div title="${usuario.apellidos}">${usuario.apellidos}</div>
      <div title="${usuario.email}">${usuario.email}</div>
      <div title="${usuario.telefono}">${usuario.telefono}</div>
      <div title="${usuario.dni}">${usuario.dni}</div>
      <div title="${usuario.direccion}">${usuario.direccion}</div>
      <div title="${usuario.localidad}">${usuario.localidad}</div>
      <div class="estado-${usuario.estado}">${usuario.estado}</div>
      <div>
        <div class="acciones">
          ${usuario.estado === 'pendiente' ? `
            <button class="aceptar" onclick="cambiarEstado(${usuario.id}, 'aceptado')">Aceptar</button>
            <button class="rechazar" onclick="cambiarEstado(${usuario.id}, 'rechazado')">Rechazar</button>
          ` : ''}
          ${usuario.estado === 'aceptado' ? `
            <button class="modificar" onclick="modificarUsuario(${usuario.id})">Modificar</button>
            <button class="baja" onclick="darDeBaja(${usuario.id})">Dar de baja</button>
          ` : ''}
        </div>
      </div>
    `;
    body.appendChild(fila);
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
  comprobarSesion();
  cargarUsuarios();
};