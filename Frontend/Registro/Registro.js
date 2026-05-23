const UrlBase = "http://127.0.0.1:8000/api/";

const api = axios.create({
  baseURL: UrlBase,
  headers: { "Content-Type": "application/json" },
});

function mostrarError(mensaje) {
  const posibleError = document.getElementById("posibleError");
  posibleError.style.display = "block";
  posibleError.innerHTML = mensaje;

  clearTimeout(posibleError._timeout);
  posibleError._timeout = setTimeout(() => {
    posibleError.style.display = "none";
  }, 8000);
}

function validarCampos(datos) {
  const soloLetras = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;
  const dniRegex = /^\d{8}[A-Za-z]$/;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const telefonoRegex = /^\d{9}$/;
  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).+$/;

  if (!datos.nombre || !datos.dni || !datos.email || !datos.password || !datos.password2) {
    mostrarError("Debes rellenar todos los campos obligatorios.");
    return false;
  }
  if (!soloLetras.test(datos.nombre)) {
    mostrarError("El nombre solo puede contener letras.");
    return false;
  }
  if (datos.apellidos && !soloLetras.test(datos.apellidos)) {
    mostrarError("Los apellidos solo pueden contener letras.");
    return false;
  }
  if (!dniRegex.test(datos.dni)) {
    mostrarError("El DNI debe tener 8 números y una letra al final (ej: 12345678A).");
    return false;
  }
  if (!emailRegex.test(datos.email)) {
    mostrarError("El email no tiene un formato válido(ej: correoejemplo@gmail.com).");
    return false;
  }
  if (datos.telefono && !telefonoRegex.test(datos.telefono)) {
    mostrarError("El teléfono debe tener 9 números.");
    return false;
  }
  if (!passwordRegex.test(datos.password)) {
    mostrarError("La contraseña debe contener letras y números.");
    return false;
  }
  if (datos.password !== datos.password2) {
    mostrarError("Las contraseñas no coinciden.");
    return false;
}

  return true;
}

async function registrarUsuario() {
  const datos = {
    nombre: document.getElementById("nombre").value,
    dni: document.getElementById("dni").value,
    apellidos: document.getElementById("apellidos").value,
    email: document.getElementById("email").value,
    telefono: document.getElementById("tlf").value,
    direccion: document.getElementById("direccion").value,
    localidad: document.getElementById("localidad").value,
    password: document.getElementById("pass").value,
    password2: document.getElementById("pass2").value,
  };

  if (!validarCampos(datos)) return;

  try {
    const respuesta = await api.post("/registro", datos);
    if (respuesta.data != null) {
      localStorage.setItem("token", respuesta.data.token);
      localStorage.setItem("usuario", JSON.stringify(respuesta.data.usuario));
      window.location.href = "../RegistroFinca/RegistroFinca.html";
    }
  } catch (error) {
    if (error.response) {
      mostrarError(error.response.data.mensaje);
    } else {
      mostrarError("Error: " + error.message);
    }
  }
}