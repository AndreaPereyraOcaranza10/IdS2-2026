package com.colegio.sistemaescolar.enums;

/**
 * Roles del sistema (autorización).
 * <ul>
 *   <li>{@link #ADMIN}: personal directivo. Gestiona docentes, grados, aulas, materias, alumnos y notas.</li>
 *   <li>{@link #DOCENTE}: ve a los alumnos de sus grados y carga notas de las materias que dicta.</li>
 * </ul>
 * Spring Security representa cada rol como la autoridad "ROLE_" + nombre (por ejemplo ROLE_ADMIN).
 */
public enum Rol {
    ADMIN("Administrador"),
    DOCENTE("Docente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
