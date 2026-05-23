const UrlBase = CONFIG.API_URL;

const api = axios.create({
  baseURL: UrlBase,
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});

function mostrarError(mensaje) {
  const posibleError = document.getElementById("posibleError");
  posibleError.style.display = "block";
  posibleError.innerHTML = mensaje;

  clearTimeout(posibleError._timeout);
  posibleError._timeout = setTimeout(() => {
    posibleError.style.display = "none";
  }, 4000);
}

function validarCampos(datos) {
  if (!datos.localidad || !datos.direccion) {
    mostrarError("Debes rellenar todos los campos obligatorios.");
    return false;
  }

  if (!datos.higueras || !datos.ciruelos || !datos.arandanos || !datos.cerezos) {
    mostrarError("Debes rellenar todos los campos obligatorios.");
    return false;
  }

  if (datos.higueras <= 0 || datos.ciruelos <= 0 || datos.arandanos <= 0 || datos.cerezos <= 0) {
    mostrarError("El número de árboles debe ser mayor que 0.");
    return false;
  }

  return true;
}

async function registrarFinca() {
  const datos = {
    localidad:  document.getElementById("localidad").value,
    direccion:  document.getElementById("direccion").value,
    higueras:   parseInt(document.getElementById("higueras").value) || 0,
    ciruelos:   parseInt(document.getElementById("ciruelos").value) || 0,
    arandanos:  parseInt(document.getElementById("arandanos").value) || 0,
    cerezos:    parseInt(document.getElementById("cerezos").value) || 0,
  };

  if (!validarCampos(datos)) return;

  const imagen = document.getElementById("imagen").files[0];
  const formData = new FormData();
  formData.append("localidad",  datos.localidad);
  formData.append("direccion",  datos.direccion);
  formData.append("higueras",   datos.higueras);
  formData.append("ciruelos",   datos.ciruelos);
  formData.append("arandanos",  datos.arandanos);
  formData.append("cerezos",    datos.cerezos);
  if (imagen) formData.append("imagen", imagen);

  try {
    const respuesta = await api.post("/fincas", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    if (respuesta.data != null) {
      window.location.href = "../Foro/Foro.html";
    }
  } catch (error) {
    console.log(error);
    if (error.response) {
      mostrarError("Error: " + error.response.data.mensaje);
    } else {
      mostrarError("Error: " + error.message);
    }
  }
}