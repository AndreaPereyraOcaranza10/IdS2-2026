package com.colegio.sistemaescolar.enums;

/**
 * Período en el que se registra una nota. El orden de las constantes es el orden
 * cronológico del ciclo lectivo (se usa para ordenar el boletín con {@code ordinal()}).
 */
public enum Periodo {
    PRIMER_TRIMESTRE("1.º trimestre"),
    SEGUNDO_TRIMESTRE("2.º trimestre"),
    TERCER_TRIMESTRE("3.º trimestre"),
    EXAMEN_FINAL("Examen final");

    private final String etiqueta;

    Periodo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
