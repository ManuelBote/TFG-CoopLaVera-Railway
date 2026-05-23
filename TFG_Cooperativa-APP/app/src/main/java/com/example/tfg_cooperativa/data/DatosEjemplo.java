package com.example.tfg_cooperativa.data;

import com.example.tfg_cooperativa.R;
import com.example.tfg_cooperativa.models.EntradaForo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class DatosEjemplo {

    private DatosEjemplo() {}

    public static List<EntradaForo> entradasForo() {
        return new ArrayList<>(Arrays.asList(
                new EntradaForo(1,
                        "Temporada de higo fresco",
                        "Agosto / Octubre",
                        R.drawable.foro_higo,
                        "Aunque las brevas (la primera cosecha de la higuera) se recogen entre " +
                                "junio y julio, los higos más dulces y verdaderos son los que maduran " +
                                "durante el calor del final del verano."),
                new EntradaForo(2,
                        "Temporada de cerezas",
                        "Mayo / Julio",
                        R.drawable.foro_cerezas,
                        "La temporada principal de cerezas en España y el hemisferio norte se concentra " +
                                "en los meses de mayo, junio y julio, siendo la primavera y el inicio del " +
                                "verano el periodo óptimo de recolección. Aunque la cosecha fuerte es en " +
                                "mayo y junio, algunas variedades tardías pueden encontrarse hasta " +
                                "principios de agosto."),
                new EntradaForo(3,
                        "Temporada de arándanos",
                        "Marzo / Agosto",
                        R.drawable.foro_arandanos,
                        "La temporada principal de arándanos en España se extiende de marzo a agosto, " +
                                "con especial calidad en los meses de verano. Aunque los primeros llegan " +
                                "de Huelva a principios de primavera, la producción nacional de mayor " +
                                "jugosidad se concentra a partir de junio, extendiéndose la recolección " +
                                "en zonas del norte como Asturias hasta octubre.")
        ));
    }
}
