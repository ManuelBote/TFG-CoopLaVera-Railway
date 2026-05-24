const UrlBase = CONFIG.API_URL;

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});


let listaEtiquetas = [];
let iconoDataUrl = '';

function precargarIcono() {
  return new Promise(resolve => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = function () {
      const canvas = document.createElement('canvas');
      canvas.width = img.naturalWidth;
      canvas.height = img.naturalHeight;
      canvas.getContext('2d').drawImage(img, 0, 0);
      iconoDataUrl = canvas.toDataURL('image/png');
      resolve();
    };
    img.onerror = () => { iconoDataUrl = ''; resolve(); };
    img.src = '../imagenes/icono.png';
  });
}

// --- Redirección si no hay token ---
function obtenerDatosUsuario() {
  const token = localStorage.getItem("token");
  const usuario = JSON.parse(localStorage.getItem("usuario"));

  if (!token) {
    window.location.href = "../Login/Login.html";
    return null;
  }

  const inputUsuario = document.getElementById("idUsuario");
  inputUsuario.value = usuario.id;
  
}


// --- Ayuda ---
const boton = document.getElementById("boton-ayuda");
const superposicion = document.getElementById("superposicion-ayuda");
const botonCerrar = document.getElementById("boton-cerrar");

boton.addEventListener("click", () => superposicion.classList.add("visible"));
botonCerrar.addEventListener("click", () =>
  superposicion.classList.remove("visible"),
);
superposicion.addEventListener("click", (evento) => {
  if (evento.target === superposicion)
    superposicion.classList.remove("visible");
});
document.addEventListener("keydown", (evento) => {
  if (evento.key === "Escape") superposicion.classList.remove("visible");
});

async function recuperarFincas() {
  try {
    const respuesta = await api.get("/fincas");
    const select = document.getElementById("nFinca");
    select.innerHTML = '<option value="">-- Selecciona una finca --</option>';
    respuesta.data.forEach((finca) => {
      select.innerHTML += `<option value="${finca.id}">${finca.id} - ${finca.localidad} / ${finca.direccion}</option>`;
    });
  } catch (error) {
    console.error("Error al recuperar las fincas:", error);
  }
}
// --- Cargar productos ---
async function recuperarProductos() {
  try {
    const respuesta = await api.get("/productos");
    const select = document.getElementById("idProducto");
    select.innerHTML = '<option value="">-- Selecciona un producto--</option>';
    respuesta.data.forEach((producto) => {
      select.innerHTML += `<option value="${producto.id}">${producto.nombre}</option>`;
    });
  } catch (error) {
    console.error("Error al recuperar los productos:", error);
  }

}

// --- Añadir a la lista ---
function anadirEtiqueta() {
  const nFinca = document.getElementById("nFinca").value.trim();
  const idUsuario = document.getElementById("idUsuario").value.trim();
  const select = document.getElementById("idProducto");
  const idProducto = select.value;
  const nombreProducto = select.options[select.selectedIndex].text;
  const cantidad = parseInt(document.getElementById("cantidad").value);

  if (!nFinca || !idUsuario || !idProducto || !cantidad || cantidad < 1) return;

  const existente = listaEtiquetas.find(
    (item) =>
      item.idProducto === idProducto &&
      item.nFinca === nFinca &&
      item.idUsuario === idUsuario,
  );

  if (existente) {
    existente.cantidad += cantidad;
  } else {
    listaEtiquetas.push({
      nFinca,
      idUsuario,
      idProducto,
      nombreProducto,
      cantidad,
    });
  }

  document.getElementById("idProducto").selectedIndex = 0;
  document.getElementById("cantidad").value = "";

  renderizarLista();
}

function renderizarLista() {
  const ul = document.getElementById("listaEtiquetas");
  ul.innerHTML = "";

  listaEtiquetas.forEach((item, index) => {
    ul.innerHTML += `
      <li>
        Finca: ${item.nFinca} | Usuario: ${item.idUsuario} | ${item.nombreProducto} | ${item.cantidad} uds.
        <button type="button" onclick="eliminarEtiqueta(${index})">✕</button>
      </li>
    `;
  });
}

function eliminarEtiqueta(index) {
  listaEtiquetas.splice(index, 1);
  renderizarLista();
}

async function imprimirEtiquetas(e) {
  e.preventDefault();

  if (listaEtiquetas.length === 0) return;

  const mapaProductos = {
    "Higo fresco": "higo_fresco",
    "Higo seco": "higo_seco",
    Arándano: "arandano",
    Cereza: "cereza",
    Ciruela: "ciruela",
  };

  try {
    // Traer datos de la finca
    const fincaResp = await api.get(`/fincas/${listaEtiquetas[0].nFinca}`);
    const finca = fincaResp.data;

    const datos = {
      idUsuario: listaEtiquetas[0].idUsuario,
      idFinca: listaEtiquetas[0].nFinca,
      higo_fresco: 0,
      higo_seco: 0,
      arandano: 0,
      cereza: 0,
      ciruela: 0,
    };

    listaEtiquetas.forEach((item) => {
      const campo = mapaProductos[item.nombreProducto];
      if (campo) datos[campo] += item.cantidad;
    });

    // Guardar en BD
    await api.post("/etiquetas", datos);

    // Generar etiquetas para imprimir
    const contenedor = document.getElementById("paginaImpresion");
    contenedor.innerHTML = "";

    listaEtiquetas.forEach((item) => {
      for (let i = 0; i < item.cantidad; i++) {
        contenedor.innerHTML += `
          <div class="etiqueta">
            <div class="etiqueta-datos">
              <h2>Cooperativa La Vera</h2>
              <p><strong>Producto:</strong> ${item.nombreProducto}</p>
              <p><strong>Nº Finca:</strong> ${item.nFinca} / ${finca.localidad} / ${finca.direccion}</p>
              <p><strong>Peso:</strong> 2.05 KG</p>
            </div>
            <div class="etiqueta-logo" style="flex:0 0 40px;width:40px;height:40px;margin-left:8px;overflow:hidden;display:flex;align-items:center;justify-content:center;">
              ${iconoDataUrl ? `<img src="${iconoDataUrl}" alt="Logo de la cooperativa" style="width:36px;height:36px;max-width:36px;max-height:36px;object-fit:contain;">` : ''}
            </div>
          </div>
        `;
      }
    });

    listaEtiquetas = [];
    renderizarLista();
    window.print();
  } catch (error) {
    console.error("Error:", error);
  }
}

window.onload = async function () {
  await precargarIcono();
  recuperarFincas();
  recuperarProductos();
  document.getElementById("btnAnadir").addEventListener("click", anadirEtiqueta);
  obtenerDatosUsuario();
  document.querySelector(".imprimir").addEventListener("click", imprimirEtiquetas);
};
