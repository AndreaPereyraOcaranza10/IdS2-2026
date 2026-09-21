package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.utils.Textos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** DTO del formulario "Cambiar contraseña". */
@Data
public class CambioPasswordDTO {

    @NotBlank(message = "Ingrese su contraseña actual.")
    private String passwordActual;

    @NotBlank(message = "Ingrese la nueva contraseña.")
    @Pattern(regexp = Textos.REGEX_PASSWORD, message = Textos.MENSAJE_PASSWORD)
    private String passwordNueva;

    @NotBlank(message = "Repita la nueva contraseña.")
    private String confirmarPassword;
}
