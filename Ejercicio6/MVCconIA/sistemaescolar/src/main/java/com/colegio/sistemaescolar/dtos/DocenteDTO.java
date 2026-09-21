package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.utils.Textos;
import com.colegio.sistemaescolar.validation.EdadValida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO de Docente para listar, ver y EDITAR. (El alta usa {@link RegistroDocenteDTO} porque incluye contraseña.)
 *
 * <p>{@code asignaciones} son las casillas marcadas en el formulario (cada una es "gradoId-materiaId");
 * {@code asignacionesTexto} es solo para mostrar.</p>
 */
@Data
public class DocenteDTO {

    private long id;

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

    /** Solo lectura: es el nombre de usuario y no se modifica desde este formulario. */
    private String email;

    /** Cuenta habilitada para ingresar (el ADMIN puede aprobar o suspender). */
    private boolean habilitado = true;

    /**
     * Asignaciones marcadas en el formulario. Cada elemento es "gradoId-materiaId" (por ejemplo "2-5" = materia 5 en
     * el grado 2). Es un texto simple para poder enlazarlo directamente con checkboxes de Thymeleaf.
     */
    private Set<String> asignaciones = new HashSet<>();

    /** Solo lectura, para el listado: "2.º Grado - Matemática". */
    private List<String> asignacionesTexto = new ArrayList<>();

    public String getNombreCompleto() {
        return (apellido == null ? "" : apellido) + ", " + (nombre == null ? "" : nombre);
    }
}
