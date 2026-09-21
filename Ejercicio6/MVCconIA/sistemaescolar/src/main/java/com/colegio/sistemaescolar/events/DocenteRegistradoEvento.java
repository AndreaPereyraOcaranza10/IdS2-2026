package com.colegio.sistemaescolar.events;

/**
 * EVENTO de dominio: "se registró un docente". Lo publica {@code ServicioDocente} y lo escucha
 * {@code EscuchaNotificaciones}, que envía el correo de bienvenida.
 *
 * <p>Usar eventos desacopla el registro (lógica de negocio) del envío de correo (efecto secundario):
 * el correo solo sale si la transacción se confirmó (AFTER_COMMIT) y un fallo de SMTP nunca
 * deshace el alta del docente.</p>
 *
 * @param email               correo personal del docente (su usuario)
 * @param nombreCompleto      "Nombre Apellido" para el saludo
 * @param pendienteAprobacion true si la cuenta debe ser habilitada por un administrador
 */
public record DocenteRegistradoEvento(String email, String nombreCompleto, boolean pendienteAprobacion) {
}
