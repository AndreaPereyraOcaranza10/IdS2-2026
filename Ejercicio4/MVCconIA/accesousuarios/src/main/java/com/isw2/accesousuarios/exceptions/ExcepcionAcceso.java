package com.isw2.accesousuarios.exceptions;

/**
 * EXCEPCIÓN DE NEGOCIO del registro y del ingreso.
 * ============================================================================
 * La capa de servicio la lanza cuando una regla de negocio impide completar la
 * operación. Lleva un {@link Motivo} para que el CONTROLADOR decida que hacer
 * (por ejemplo, redirigir al registro si el usuario no existe) sin tener que
 * interpretar el texto del mensaje.
 *
 * Es una excepción CHECKED (extiende Exception): el compilador obliga a
 * manejarla, coherente con la convención del proyecto de declarar
 * "throws Exception" en los servicios.
 *
 * OJO con las transacciones: por defecto Spring hace rollback solo ante
 * excepciones NO checked. Aquí se usa noRollbackFor en el servicio para que el
 * contador de intentos fallidos se guarde igual aunque se lance esta excepción.
 */
public class ExcepcionAcceso extends Exception {

    private static final long serialVersionUID = 1L;

    /** Causa del rechazo. */
    public enum Motivo {
        /** Ingreso: el correo no pertenece a ningun usuario registrado. */
        NO_REGISTRADO,
        /** Ingreso: la clave es incorrecta (todavía quedan intentos). */
        CLAVE_INCORRECTA,
        /** Ingreso: la cuenta está bloqueada (o se acaba de bloquear). */
        CUENTA_BLOQUEADA,
        /** Registro: la clave y su confirmación no coinciden. */
        CLAVES_NO_COINCIDEN,
        /** Registro: ya existe una persona con ese correo. */
        CORREO_DUPLICADO,
        /** Registro: ya existe una persona con ese documento. */
        DOCUMENTO_DUPLICADO
    }

    private final Motivo motivo;

    public ExcepcionAcceso(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
