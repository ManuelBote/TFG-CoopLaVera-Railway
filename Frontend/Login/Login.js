const UrlBase = CONFIG.API_URL;

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

async function loginUsuario() {
  const datos = {
    email: document.getElementById("email").value,
    pass: document.getElementById("pass").value,
  };

  try {
    const respuesta = await api.post("/login", datos);
    if (respuesta.data != null) {
      console.log(respuesta.data);
      localStorage.setItem("token", respuesta.data.token);
      localStorage.setItem("usuario", JSON.stringify(respuesta.data.usuario));

      if (respuesta.data.usuario.tipo === 2) {
        window.location.href = "../InicioAdministrador/InicioAdministrador.html";
      } else {
        window.location.href = "../Foro/Foro.html";
      }
    }
  } catch (error) {
    if (error.response) {
      if (error.response.status === 422) {
        mostrarError("Debes rellenar todos los campos.");
      } else {
        mostrarError(error.response.data.mensaje);
      }
    } else {
      mostrarError("Error: " + error.message);
    }
  }
}