const UrlBase = CONFIG.API_URL;

const api = axios.create({
    baseURL: UrlBase,
    headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + localStorage.getItem("token")
    }
});

if (!token) {
    window.location.href = "../Login/Login.html";
}

function cerrarSesion() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "../Login/Login.html";
}

async function comprobarEstado() {
    try {
        const respuesta = await api.get("/perfil");
        const usuarioActualizado = respuesta.data.usuario;
        const usuarioActual = JSON.parse(localStorage.getItem("usuario"));
        localStorage.setItem("usuario", JSON.stringify({
            ...usuarioActual,
            estado: usuarioActualizado.estado
        }));
        if (usuarioActualizado.estado === "aceptado") {
            window.location.href = "../PanelUsuario/PanelUsuario.html";
        } else {
            alert("Tu cuenta sigue pendiente de aprobación.");
        }
    } catch (error) {
        console.error("Error al comprobar estado:", error);
    }
}
