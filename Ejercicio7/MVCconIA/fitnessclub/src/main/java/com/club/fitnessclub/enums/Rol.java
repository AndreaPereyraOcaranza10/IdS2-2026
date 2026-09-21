package com.club.fitnessclub.enums;

/**
 * Roles de seguridad.
 * <ul>
 *   <li>ADMIN: gestión completa (familias, personas, cuotas, pagos, usuarios).</li>
 *   <li>RECEPCION: registra entradas/salidas y consulta personas; sin acceso a pagos ni usuarios.</li>
 * </ul>
 * Spring Security los expone como "ROLE_ADMIN" / "ROLE_RECEPCION".
 */
public enum Rol {
    ADMIN("Administrador"),
    RECEPCION("Recepción");

    private final String etiqueta;

    Rol(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
