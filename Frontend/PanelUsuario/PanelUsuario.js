const UrlBase = "http://127.0.0.1:8000/api/";

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

  if (!token) { window.location.href = "../Login/Login.html"; return null; }
  if (usuario.estado !== "aceptado") { window.location.href = "../Pendiente/pendiente.html"; return null; }

  document.getElementById("nombre").textContent = "Nombre: " + usuario.nombre;
  document.getElementById("idUsuario").textContent = "Id Usuario: " + usuario.id;
  document.getElementById("tlf").textContent = "Tlf: " + usuario.telefono;
  document.getElementById("email").textContent = "Email: " + usuario.correo;

  if (usuario.tipo == 2) {
    const enlaces = document.getElementById("enlaces");
    const enlaceAdmin = document.createElement("a");
    enlaceAdmin.href = "../InicioAdministrador/InicioAdministrador.html";
    enlaceAdmin.innerHTML = `<div class="enlace enlace-admin">Panel Admin</div>`;
    enlaces.appendChild(enlaceAdmin);
  }

  return { token, usuario };
}

// ── ENTREGAS ──────────────────────────────────────────────

let entregasData = {};
let graficoEntregas = null;

async function inicializarGraficoEntregas() {
  try {
    const respuesta = await api.get("/entregas");
    entregasData = respuesta.data.entregas;

    // Lista de totales
    const lista = document.getElementById("listaEntregas");
    lista.innerHTML = "";
    Object.keys(entregasData).forEach(nombre => {
      const total = entregasData[nombre].reduce((s, e) => s + e.cantidad, 0);
      lista.innerHTML += `<li><strong>${nombre}:</strong> ${total} kilos</li>`;
    });

    // Rellenar select de años
    const anios = [...new Set(
      Object.values(entregasData).flat().map(e => e.fecha.substring(0, 4))
    )].sort();
    const selectAnio = document.getElementById("filtroAnioEntregas");
    anios.forEach(a => selectAnio.innerHTML += `<option value="${a}">${a}</option>`);

    renderizarGraficoEntregas();
  } catch (error) {
    console.log("Error al cargar entregas:", error);
  }
}

function filtrarGraficoEntregas() {
  const mes = document.getElementById("filtroMesEntregas").value;
  const anio = document.getElementById("filtroAnioEntregas").value;
  renderizarGraficoEntregas(mes, anio);
}

function renderizarGraficoEntregas(mes = null, anio = null) {
  const colores = ["green", "blue", "red", "orange", "purple", "pink"];

  const datasets = Object.keys(entregasData).map((nombre, index) => {
    let registros = entregasData[nombre];
    if (anio) registros = registros.filter(e => e.fecha.substring(0, 4) === anio);
    if (mes)  registros = registros.filter(e => e.fecha.substring(5, 7) === mes);
    return {
      label: nombre,
      data: registros.map(e => e.cantidad),
      borderColor: colores[index % colores.length],
      fill: false,
      stepped: true,
    };
  });

  const todasFechas = [...new Set(
    Object.values(entregasData).flat()
      .filter(e => (!anio || e.fecha.substring(0, 4) === anio) && (!mes || e.fecha.substring(5, 7) === mes))
      .map(e => e.fecha)
  )].sort();

  if (graficoEntregas) graficoEntregas.destroy();

  const ctx = document.getElementById("miGrafico2").getContext("2d");
  graficoEntregas = new Chart(ctx, {
    type: "line",
    data: { labels: todasFechas, datasets },
    options: {
      scales: {
        y: { min: 0, max: 1000, ticks: { stepSize: 100 } },
      },
    },
  });
}

let historialPrecios = {};
let graficoPrecio = null;

async function inicializarGraficoPreciosProductos() {
  try {
    const respuesta = await api.get("/historial-precios");
    historialPrecios = respuesta.data;

    const selectProducto = document.getElementById("filtroProducto");
    Object.keys(historialPrecios).forEach(nombre => {
      selectProducto.innerHTML += `<option value="${nombre}">${nombre}</option>`;
    });

    const anios = [...new Set(
      Object.values(historialPrecios).flat().map(r => r.fecha.substring(0, 4))
    )].sort();
    const selectAnio = document.getElementById("filtroAnioPrecio");
    anios.forEach(a => selectAnio.innerHTML += `<option value="${a}">${a}</option>`);

    const primero = Object.keys(historialPrecios)[0];
    if (primero) {
      selectProducto.value = primero;
      renderizarGraficoPrecio(primero);
    }
  } catch (error) {
    console.error("Error al cargar historial de precios:", error);
  }
}

function filtrarGraficoPrecio() {
  const producto = document.getElementById("filtroProducto").value;
  const mes = document.getElementById("filtroMesPrecio").value;
  const anio = document.getElementById("filtroAnioPrecio").value;
  if (producto) renderizarGraficoPrecio(producto, mes, anio);
}

function renderizarGraficoPrecio(nombreProducto, mes = null, anio = null) {
  let registros = historialPrecios[nombreProducto];
  if (anio) registros = registros.filter(r => r.fecha.substring(0, 4) === anio);
  if (mes)  registros = registros.filter(r => r.fecha.substring(5, 7) === mes);

  const datasets = [
    { label: "Congelado", data: registros.map(r => r.precio_congelado), borderColor: "blue",   fill: false, stepped: true },
    { label: "M",         data: registros.map(r => r.precio_m),         borderColor: "green",  fill: false, stepped: true },
    { label: "L",         data: registros.map(r => r.precio_l),         borderColor: "orange", fill: false, stepped: true },
    { label: "Jumbo",     data: registros.map(r => r.precio_jumbo),     borderColor: "red",    fill: false, stepped: true },
  ];

  if (graficoPrecio) graficoPrecio.destroy();

  const ctx = document.getElementById("precioProductos").getContext("2d");
  graficoPrecio = new Chart(ctx, {
    type: "line",
    data: { labels: registros.map(r => r.fecha), datasets },
    options: {
      scales: {
        y: { min: 0, max: 5, ticks: { stepSize: 0.5 } },
      },
    },
  });
}

async function inicializarMaterialAlquilado() {
  try {
    const respuesta = await api.get("/mis-materiales");
    const materiales = respuesta.data.materiales;
    const lista = document.getElementById("listaMaterial");
    lista.innerHTML = "";
    Object.keys(materiales).forEach(nombre => {
      lista.innerHTML += `<li>${nombre}: ${materiales[nombre]} unidades</li>`;
    });
  } catch (error) {
    console.log("Error al cargar materiales:", error);
  }
}

// ── INIT ──────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", () => {
  obtenerDatosUsuario();
  inicializarGraficoEntregas();
  inicializarGraficoPreciosProductos();
  inicializarMaterialAlquilado();
});