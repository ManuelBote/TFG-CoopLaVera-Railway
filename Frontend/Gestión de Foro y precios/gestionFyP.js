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

async function cargarProductos() {
  try {
    const respuesta = await api.get("/productos");
    const productos = respuesta.data;
    const contenedor = document.getElementById("contenedorProductos");
    contenedor.innerHTML = "";
    productos.forEach(producto => {
      contenedor.innerHTML += `
        <div class="producto" data-id="${producto.id}">
          <h1>Producto: ${producto.nombre}</h1>
          <div class="infoProd" data-campo="precio_congelado">
            <h2>Precio congelados:</h2>
            <h2 class="precio-valor">${producto.precio_congelado} €/kg</h2>
          </div>
          <div class="infoProd" data-campo="precio_m">
            <h2>Precio m:</h2>
            <h2 class="precio-valor">${producto.precio_m} €/kg</h2>
          </div>
          <div class="infoProd" data-campo="precio_l">
            <h2>Precio l:</h2>
            <h2 class="precio-valor">${producto.precio_l} €/kg</h2>
          </div>
          <div class="infoProd" data-campo="precio_jumbo">
            <h2>Precio jumbo:</h2>
            <h2 class="precio-valor">${producto.precio_jumbo} €/kg</h2>
          </div>
          <div class="producto-botones">
            <button class="editar" onclick="activarEdicion(this)">Editar</button>
          </div>
        </div>
      `;
    });

  } catch (error) {
    console.error("Error al cargar productos:", error);
  }
}

function activarEdicion(btn) {
  const producto = btn.closest('.producto');
  const botones = btn.parentElement;
  const valoresOriginales = {};

  producto.querySelectorAll('.infoProd').forEach(fila => {
    const campo = fila.dataset.campo;
    const valorActual = fila.querySelector('.precio-valor').textContent.replace(' €/kg', '').trim();
    valoresOriginales[campo] = valorActual;

    fila.querySelector('.precio-valor').innerHTML = `
      <input type="number" class="precio-input" data-campo="${campo}" value="${valorActual}" step="0.01">
      €/kg
    `;
  });

  btn.textContent = 'Guardar';
  btn.className = 'guardar';
  btn.onclick = () => guardarPrecios(btn);

  const cancelar = document.createElement('button');
  cancelar.textContent = 'Cancelar';
  cancelar.className = 'cancelar';
  cancelar.onclick = () => {
    producto.querySelectorAll('.infoProd').forEach(fila => {
      const campo = fila.dataset.campo;
      fila.querySelector('.precio-valor').textContent = `${valoresOriginales[campo]} €/kg`;
    });
    btn.textContent = 'Editar';
    btn.className = 'btn-editar';
    btn.onclick = () => activarEdicion(btn);
    cancelar.remove();
  };
  botones.appendChild(cancelar);
}

async function guardarPrecios(btn) {
  const producto = btn.closest('.producto');
  const id = producto.dataset.id;
  const datos = {};

  producto.querySelectorAll('.precio-input').forEach(input => {
    datos[input.dataset.campo] = parseFloat(input.value);
  });

  try {
    await api.put(`/productos/${id}`, datos);
    await cargarProductos();
  } catch (error) {
    console.error("Error al guardar:", error);
  }
}

document.addEventListener("DOMContentLoaded", cargarProductos);