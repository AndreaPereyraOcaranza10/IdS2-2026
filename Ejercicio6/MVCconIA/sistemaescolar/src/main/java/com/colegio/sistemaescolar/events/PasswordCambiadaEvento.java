package com.colegio.sistemaescolar.events;

/**
 * EVENTO de dominio: "un usuario cambió su contraseña". Dispara un correo de aviso de seguridad
 * (si el cambio no fue hecho por el titular, puede reaccionar).
 *
 * @param email          correo del usuario
 * @param nombreCompleto nombre para el saludo (o el email si no hay ficha de docente)
 */
public record PasswordCambiadaEvento(String email, String nombreCompleto) {
}
