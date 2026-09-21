package com.club.fitnessclub.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.club.fitnessclub.enums.Parentesco;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Persona (socio titular o familiar).
 *
 * <p>La FAMILIA viaja como id (grupoFamiliarId), no como objeto: la vista solo necesita saber
 * a cuál pertenece, y así se evita exponer la entidad.
 * La FOTO no va en este DTO: se sube/descarga por separado con ImagenRostroDTO (los bytes no
 * deben viajar en cada listado). Aquí solo se indica si existe (tieneRostro).
 */
@Data
@NoArgsConstructor
public class PersonaDTO {

    private long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
    private String apellido;

    /** Solo dígitos, sin puntos (7 a 9 dígitos). */
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,9}", message = "El DNI debe tener entre 7 y 9 dígitos, sin puntos")
    private String dni;

    /**
     * @DateTimeFormat: permite que un input type="date" (formato ISO yyyy-MM-dd)
     * se convierta a LocalDate al enviar el formulario.
     */
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @Email(message = "El email no es válido")
    @Size(max = 120, message = "El email no puede superar los 120 caracteres")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @NotNull(message = "Debe indicar el parentesco")
    private Parentesco parentesco;

    /**
     * Familia existente. Puede ser null si se está creando una familia nueva (ver nuevaFamiliaNombre).
     * La condición "una de las dos" la valida ServicioPersona.
     */
    private Long grupoFamiliarId;

    /** Nombre de una familia NUEVA a crear junto con el titular (solo en el alta). */
    @Size(max = 100, message = "El nombre de la familia no puede superar los 100 caracteres")
    private String nuevaFamiliaNombre;

    // ----- Solo lectura (calculados por el mapeador) -----

    private String grupoFamiliarNombre;

    /** true si la persona ya tiene foto de rostro cargada. */
    private boolean tieneRostro;

    private boolean activo = true;

    /** Utilidad para vistas: "Apellido, Nombre". */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}
