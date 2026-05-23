let totalSlides = document.querySelectorAll(".slide").length;
let actual = 0;

let track = document.getElementById("track");
let btnIzq = document.getElementById("btnIzq");
let btnDer = document.getElementById("btnDer");

// Crear puntos indicadores dinámicamente



function actualizarUI() {
  track.style.transform = `translateX(-${actual * 100}%)`;
  btnIzq.disabled = actual === 0;
  btnDer.disabled = actual === totalSlides - 1;
  document.querySelectorAll(".dot").forEach((d, i) => {
    d.classList.toggle("activo", i === actual);
  });
}

function mover(dir) {
  actual = Math.max(0, Math.min(totalSlides - 1, actual + dir));
  actualizarUI();
}

function irA(idx) {
  actual = idx;
  actualizarUI();
}

actualizarUI();

function toggleMenu() {
  document.getElementById("enlaces").classList.toggle("open");
}

document.querySelectorAll(".header-enlaces a").forEach((link) => {
  link.addEventListener("click", () => {
    document.getElementById("enlaces").classList.remove("open");
  });
});
document.addEventListener("DOMContentLoaded", function() {
    let token = localStorage.getItem("token");
    console.log("Token:", token);
    if (token) {
        document.getElementById("inicioSesion").style.display = "none";
        document.getElementById("panel").style.display = "grid";
    } else {
        document.getElementById("inicioSesion").style.display = "grid";
        document.getElementById("panel").style.display = "none";
    }
});
