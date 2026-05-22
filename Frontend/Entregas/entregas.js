const UrlBase = "http://127.0.0.1:8000/api/";

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"), 
  },
});

function obtenerDatosUsuario() {
  const token = localStorage.getItem("token");
  const usuario = JSON.parse(localStorage.getItem("usuario"));

  if (!token) {
    window.location.href = "../Login/Login.html";
    return null;
  }
  let idSocio = document.getElementById("idSocio");
  idSocio.value = usuario.id;
}

const ahora = new Date();
ahora.setMinutes(ahora.getMinutes() - ahora.getTimezoneOffset());
document.getElementById("fechaEntrega").value = ahora.toISOString().slice(0, 16);

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

async function recuperarProductos() {
  try {
    const respuesta = await api.get("/productos");
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

async function agregarEntrega() {
  const congelados = parseInt(document.getElementById("cCongelados").value) || 0;
  const m = parseInt(document.getElementById("cM").value) || 0;
  const l = parseInt(document.getElementById("cL").value) || 0;
  const jumbo = parseInt(document.getElementById("cJumbo").value) || 0;

  if (congelados + m + l + jumbo === 0) {
    mostrarMensaje("Debes introducir al menos una calidad.", "error");
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
      mostrarMensaje("Entrega registrada correctamente", "exito");
      document.getElementById("productos").selectedIndex = 0;
      document.getElementById("cCongelados").value = "";
      document.getElementById("cM").value = "";
      document.getElementById("cL").value = "";
      document.getElementById("cJumbo").value = "";
    }
  } catch (error) {
    if (error.response) {
      mostrarMensaje(error.response.data.mensaje, "error");
    } else {
      mostrarMensaje("Error: " + error.message, "error");
    }
  }
}

function mostrarMensaje(mensaje, tipo) {
  const el = document.getElementById("infomensaje");
  el.innerHTML = `<h3>${mensaje}</h3>`;
  el.className = tipo === "exito" ? "mensaje mensaje-exito" : "mensaje mensaje-error";
  el.style.display = "block";
  setTimeout(() => {
    ocultarMensaje();
  }, 8000);
}

function ocultarMensaje() {
  const el = document.getElementById("infomensaje");
  el.style.display = "none";
  el.innerHTML = "";
}
document.addEventListener("DOMContentLoaded", () => {
  recuperarProductos();
  obtenerDatosUsuario();
});
