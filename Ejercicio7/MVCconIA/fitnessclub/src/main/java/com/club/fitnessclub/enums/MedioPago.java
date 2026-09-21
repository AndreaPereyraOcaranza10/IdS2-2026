package com.club.fitnessclub.enums;

/** Medios de pago admitidos para abonar la cuota del club. */
public enum MedioPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    MERCADO_PAGO("Mercado Pago");

    private final String etiqueta;

    MedioPago(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    /** Los medios electrónicos exigen un comprobante / ID de operación (regla de negocio). */
    public boolean requiereReferencia() {
        return this != EFECTIVO;
    }
}
