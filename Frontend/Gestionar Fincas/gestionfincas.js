const UrlBase = CONFIG.API_URL;

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    "Authorization": "Bearer " + localStorage.getItem("token")
  },
});
function obtenerDatosUsuario() {
  const token = localStorage.getItem("token");
  const usuario = JSON.parse(localStorage.getItem("usuario"));

  if (!token) {
    window.location.href = "../Login/Login.html";
    return null;
  }
}

async function cargarFincas() {
  try {
    const respuesta = await api.get("/fincas");
    const fincas = respuesta.data;

    const contenedor = document.getElementById("contenedorFincas");
    contenedor.innerHTML = "";

    fincas.forEach(finca => {
      contenedor.innerHTML += `
        <div class="finca" data-id="${finca.id}">
          <div class="finca-info">
            <h1>Finca #${finca.id}</h1>
            <div class="finca-fila" data-campo="localidad">
              <h2>Localidad: <span class="finca-valor">${finca.localidad}</span></h2>
            </div>
            <div class="finca-fila" data-campo="direccion">
              <h2>Dirección: <span class="finca-valor">${finca.direccion}</span></h2>
            </div>
            <div class="finca-fila" data-campo="higueras">
              <h2>Higueras: <span class="finca-valor">${finca.higueras}</span></h2>
            </div>
            <div class="finca-fila" data-campo="ciruelos">
              <h2>Ciruelos: <span class="finca-valor">${finca.ciruelos}</span></h2>
            </div>
            <div class="finca-fila" data-campo="arandanos">
              <h2>Arándanos: <span class="finca-valor">${finca.arandanos}</span></h2>
            </div>
            <div class="finca-fila" data-campo="cerezos">
              <h2>Cerezos: <span class="finca-valor">${finca.cerezos}</span></h2>
            </div>
            <div class="finca-botones">
              <button class="editar" onclick="activarEdicion(this)">Editar</button>
              <button class="eliminar" onclick="eliminarFinca(${finca.id})">Eliminar</button>
            </div>
          </div>
        </div>
      `;
    });

  } catch (error) {
    console.error("Error al cargar fincas:", error);
  }
}

function activarEdicion(btn) {
  const finca = btn.closest(".finca");
  const id = finca.dataset.id;

  finca.innerHTML = `
    <div class="finca-info">
      <h1>Finca #${id}</h1>
      <div class="finca-fila" data-campo="localidad">
        <h2>Localidad:</h2>
        <input class="finca-input" type="text" id="editLocalidad" value="${finca.querySelector('[data-campo="localidad"] .finca-valor').textContent}" />
      </div>
      <div class="finca-fila" data-campo="direccion">
        <h2>Dirección:</h2>
        <input class="finca-input" type="text" id="editDireccion" value="${finca.querySelector('[data-campo="direccion"] .finca-valor').textContent}" />
      </div>
      <div class="finca-fila" data-campo="higueras">
        <h2>Higueras:</h2>
        <input class="finca-input" type="number" id="editHigueras" value="${finca.querySelector('[data-campo="higueras"] .finca-valor').textContent}" />
      </div>
      <div class="finca-fila" data-campo="ciruelos">
        <h2>Ciruelos:</h2>
        <input class="finca-input" type="number" id="editCiruelos" value="${finca.querySelector('[data-campo="ciruelos"] .finca-valor').textContent}" />
      </div>
      <div class="finca-fila" data-campo="arandanos">
        <h2>Arándanos:</h2>
        <input class="finca-input" type="number" id="editArandanos" value="${finca.querySelector('[data-campo="arandanos"] .finca-valor').textContent}" />
      </div>
      <div class="finca-fila" data-campo="cerezos">
        <h2>Cerezos:</h2>
        <input class="finca-input" type="number" id="editCerezos" value="${finca.querySelector('[data-campo="cerezos"] .finca-valor').textContent}" />
      </div>
      <div class="finca-botones">
        <button class="guardar" onclick="guardarFinca(${id})">Guardar</button>
        <button class="cancelar" onclick="cargarFincas()">Cancelar</button>
      </div>
    </div>
  `;
  finca.classList.add("editando");
}

async function guardarFinca(id) {
  const datos = {
    localidad: document.getElementById("editLocalidad").value,
    direccion: document.getElementById("editDireccion").value,
    higueras:  document.getElementById("editHigueras").value,
    ciruelos:  document.getElementById("editCiruelos").value,
    arandanos: document.getElementById("editArandanos").value,
    cerezos:   document.getElementById("editCerezos").value,
  };

  try {
    await api.put(`/fincas/${id}`, datos);
    cargarFincas();
  } catch (error) {
    console.error("Error al guardar finca:", error);
    alert("Error al guardar los datos");
  }
}



async function eliminarFinca(id) {
  if (!confirm("¿Seguro que quieres eliminar esta finca?")) return;
  try {
    await api.delete(`/fincas/${id}`);
    cargarFincas();
  } catch (error) {
    console.error("Error al eliminar finca:", error);
    alert("Error al eliminar la finca");
  }
}

document.addEventListener("DOMContentLoaded", cargarFincas);