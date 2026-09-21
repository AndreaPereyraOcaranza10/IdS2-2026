package com.club.fitnessclub.exceptions;

/**
 * Excepción de NEGOCIO: se lanza cuando se viola una regla del dominio
 * (DNI duplicado, segundo titular en una familia, imagen inválida, etc.).
 *
 * <p>Es CHECKED (extiende Exception) para ser coherente con ServicioBase, que
 * declara "throws Exception". Su mensaje está pensado para mostrarse al usuario
 * (el controlador lo pasa a la vista). Cualquier otra excepción se considera un
 * error inesperado del sistema.
 *
 * <p>IMPORTANTE: al ser checked, los servicios usan
 * {@code @Transactional(rollbackFor = Exception.class)}; de lo contrario Spring NO
 * revertiría la transacción cuando se lance.
 */
public class ExcepcionNegocio extends Exception {

    private static final long serialVersionUID = 1L;

    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
    }
}
