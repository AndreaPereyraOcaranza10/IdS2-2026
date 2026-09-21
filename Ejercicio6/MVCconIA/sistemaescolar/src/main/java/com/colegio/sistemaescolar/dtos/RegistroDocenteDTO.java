package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.utils.Textos;
import com.colegio.sistemaescolar.validation.EdadValida;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO del REGISTRO de un docente (formulario público /registro y alta desde el panel).
 *
 * <p>Datos pedidos en el enunciado: Nombre, Apellido, Sexo y Fecha de Nacimiento. Además el correo
 * personal (que será su nombre de usuario) y la contraseña con su confirmación.</p>
 */
@Data
public class RegistroDocenteDTO {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "Máximo 60 caracteres.")
    @Pattern(regexp = Textos.REGEX_NOMBRE, message = "Solo letras, espacios, punto, apóstrofe y guion.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 60, message = "Máximo 60 caracteres.")
    @Pattern(regexp = Textos.REGEX_NOMBRE, message = "Solo letras, espacios, punto, apóstrofe y guion.")
    private String apellido;

    @NotNull(message = "Seleccione el sexo.")
    private Sexo sexo;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
    @EdadValida(min = 18, max = 100, message = "El docente debe ser mayor de edad (18 años o más).")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo personal es obligatorio.")
    @Email(message = "Ingrese un correo válido.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Pattern(regexp = Textos.REGEX_PASSWORD, message = Textos.MENSAJE_PASSWORD)
    private String password;

    @NotBlank(message = "Repita la contraseña.")
    private String confirmarPassword;
}
