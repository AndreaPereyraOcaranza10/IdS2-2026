package com.club.fitnessclub.enums;

/**
 * Estado de una cuota. "Vencida" NO es un estado persistido: se calcula
 * (PENDIENTE y fechaVencimiento anterior a hoy), así nunca queda desactualizado.
 */
public enum EstadoCuota {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    ANULADA("Anulada");

    private final String etiqueta;

    EstadoCuota(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
