package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.utils.Textos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO de Alumno. Además de los datos del formulario incluye campos de solo lectura
 * ({@code gradoNombre}, {@code aulaCodigo}, {@code edad}) que el mapeador completa para mostrar en las listas.
 *
 * <p>{@code @DateTimeFormat(iso = DATE)} indica que la fecha llega como "aaaa-MM-dd", que es el formato
 * que envía el {@code <input type="date">} del navegador.</p>
 */
@Data
public class AlumnoDTO {

    private long id;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos, sin puntos.")
    private String dni;

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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String domicilio;

    @Size(max = 120, message = "Máximo 120 caracteres.")
    private String nombreTutor;

    @Pattern(regexp = "^$|^[0-9+()\\- ]{6,30}$", message = "Teléfono inválido (solo números, +, paréntesis y guiones).")
    private String telefonoTutor;

    @NotNull(message = "Seleccione el grado.")
    private Long gradoId;

    @NotNull(message = "Seleccione el aula.")
    private Long aulaId;

    // ----- Campos de solo lectura (los completa el mapeador) -----
    private String gradoNombre;
    private String aulaCodigo;
    private int edad;

    public String getNombreCompleto() {
        return (apellido == null ? "" : apellido) + ", " + (nombre == null ? "" : nombre);
    }
}
