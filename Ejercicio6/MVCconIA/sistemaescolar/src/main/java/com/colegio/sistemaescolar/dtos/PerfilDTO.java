package com.colegio.sistemaescolar.dtos;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de solo lectura para la pantalla "Mi perfil": datos de la cuenta con la que se inició sesión.
 * Se arma en {@code ServicioCuenta} y viaja a la vista; la entidad {@code Usuario} nunca sale de la capa de servicio.
 */
@Data
public class PerfilDTO {
    private String email;
    private String rolEtiqueta;
    /** Nombre y apellido (el ADMIN inicial no tiene ficha de docente, por eso puede venir un texto genérico). */
    private String nombreCompleto;
    private LocalDateTime ultimoCambioPassword;
}
