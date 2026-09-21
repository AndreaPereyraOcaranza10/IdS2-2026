package com.colegio.sistemaescolar.enums;

/**
 * Sexo de una persona (alumno o docente).
 * Se guarda en la base como texto (@Enumerated(EnumType.STRING)) para que sea legible y
 * no dependa del orden de las constantes. {@code etiqueta} es el texto que ve el usuario.
 */
public enum Sexo {
    FEMENINO("Femenino"),
    MASCULINO("Masculino"),
    OTRO("Otro");

    private final String etiqueta;

    Sexo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
