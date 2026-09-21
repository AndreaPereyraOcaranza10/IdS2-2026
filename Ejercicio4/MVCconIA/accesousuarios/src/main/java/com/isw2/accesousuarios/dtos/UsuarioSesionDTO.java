package com.isw2.accesousuarios.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * CAPA: DTO (Data Transfer Object) - datos del usuario que INICIO SESIÓN
 * ============================================================================
 * Es lo único que se guarda en la sesión HTTP tras un ingreso exitoso y lo que
 * usan las vistas para saludar al usuario. Deliberadamente NO incluye la clave
 * (ni su hash) ni datos de bloqueo.
 *
 * Serializable: los objetos guardados en la sesión HTTP deben poder serializarse
 * (el servidor podría persistir o replicar sesiones).
 *
 * @AllArgsConstructor (Lombok): constructor con todos los campos, que usa el mapper.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSesionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String apellido;
    private String correo;
}
