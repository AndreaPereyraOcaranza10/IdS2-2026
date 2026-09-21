package com.isw2.accesousuarios.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CAPA: DTO (Data Transfer Object) - datos del FORMULARIO DE INGRESO
 * ============================================================================
 * Enlazado con el formulario /login. El "usuario" es el correo personal.
 * Ver {@link RegistroDTO} para el detalle de las anotaciones de Lombok y de
 * validación.
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Ingrese su correo personal.")
    @Email(message = "Ingrese un correo electrónico válido.")
    private String correo;

    /** El máximo de 32 coincide con el del registro (limite de BCrypt). */
    @NotBlank(message = "Ingrese su clave.")
    @Size(max = 32, message = "La clave no puede superar los 32 caracteres.")
    private String clave;
}
