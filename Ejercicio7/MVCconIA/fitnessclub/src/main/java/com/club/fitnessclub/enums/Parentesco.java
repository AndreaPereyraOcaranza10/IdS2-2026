package com.club.fitnessclub.enums;

/** Rol de una persona dentro de su grupo familiar. Se persiste como texto (EnumType.STRING). */
public enum Parentesco {
    TITULAR("Titular"),
    CONYUGE("Cónyuge"),
    HIJO("Hijo/a"),
    OTRO("Otro");

    private final String etiqueta;

    Parentesco(String etiqueta) { this.etiqueta = etiqueta; }

    /** Texto legible para mostrar en las vistas Thymeleaf. */
    public String getEtiqueta() { return etiqueta; }
}
