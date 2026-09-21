package com.colegio.sistemaescolar.utils;

/**
 * Utilidades de texto usadas por mapeadores y servicios.
 */
public final class Textos {

    /** Expresión regular para nombres y apellidos: letras (con acentos), espacios, punto, apóstrofe y guion. */
    public static final String REGEX_NOMBRE = "^[\\p{L}][\\p{L} .'-]*$";

    /**
     * Política de contraseña: entre 8 y 72 caracteres, al menos una letra y un número.
     * (72 es el máximo que BCrypt tiene en cuenta.)
     */
    public static final String REGEX_PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";

    public static final String MENSAJE_PASSWORD =
            "La contraseña debe tener entre 8 y 72 caracteres, con al menos una letra y un número.";

    private Textos() {
        // clase de utilidades: no se instancia
    }

    /** Quita espacios de los extremos; si queda vacío devuelve null (así la base guarda NULL y no ""). */
    public static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String recortado = texto.trim();
        return recortado.isEmpty() ? null : recortado;
    }

    /** Igual que {@link #limpiar(String)} pero nunca devuelve null (devuelve cadena vacía). */
    public static String limpiarNoNulo(String texto) {
        String limpio = limpiar(texto);
        return limpio == null ? "" : limpio;
    }
}
