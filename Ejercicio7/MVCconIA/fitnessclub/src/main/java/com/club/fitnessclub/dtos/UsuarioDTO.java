package com.club.fitnessclub.dtos;

import com.club.fitnessclub.enums.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Usuario del sistema.
 *
 * <p>VALIDACIÓN CONDICIONAL de la contraseña: en el alta es obligatoria; en la modificación es
 * opcional (vacía = se conserva la actual). Como Bean Validation no puede expresar esa condición
 * con un campo simple, la regla se valida en ServicioUsuario. El hash NUNCA se copia al DTO.
 */
@Data
@NoArgsConstructor
public class UsuarioDTO {

    private long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Solo letras, números, punto, guion y guion bajo")
    private String username;

    /** Contraseña en texto plano SOLO mientras viaja del formulario al servicio; se hashea con BCrypt. */
    @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres")
    private String password;

    private String confirmarPassword;

    @NotNull(message = "Debe seleccionar un rol")
    private Rol rol;

    private boolean activo = true;
}
