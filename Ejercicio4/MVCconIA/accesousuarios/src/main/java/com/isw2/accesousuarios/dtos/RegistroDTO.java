package com.isw2.accesousuarios.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * CAPA: DTO (Data Transfer Object) - datos del FORMULARIO DE REGISTRO
 * ============================================================================
 * Objeto que Thymeleaf enlaza con el formulario /registro (th:object) y que el
 * controlador recibe con @Valid. Contiene los datos personales pedidos por el
 * enunciado mas la clave y su confirmación (que NO forman parte de Persona).
 *
 * Por que un DTO y no la entidad: la vista nunca debe tocar entidades JPA. El
 * DTO expone solo lo que el formulario necesita y concentra las validaciones
 * de FORMATO (el "que" ingreso el usuario); las reglas de NEGOCIO (correo
 * repetido, claves distintas) las resuelve el servicio.
 *
 * Implementa Serializable porque puede viajar como "flash attribute" en la
 * redirección desde el login (correo precargado) y se guarda en la sesión HTTP.
 *
 * ANOTACIONES DE CLASE (Lombok)
 *   @Getter/@Setter   : Thymeleaf y Spring MVC leen/escriben los campos por
 *                       getters/setters.
 *   @NoArgsConstructor: Spring MVC instancia el DTO vacio antes de enlazar el form.
 *
 * ANOTACIONES DE VALIDACIÓN (jakarta.validation). Se ejecutan al usar @Valid en el
 * controlador; si fallan, el error queda en el BindingResult y la vista lo muestra
 * junto al campo con th:errors.
 *   @NotBlank : no nulo, ni vacio, ni solo espacios (para String).
 *   @NotNull  : no nulo (para tipos no String, como la fecha).
 *   @Size     : largo mínimo/máximo.
 *   @Email    : formato de correo electrónico.
 *   @Past     : la fecha debe ser anterior a hoy.
 *   @Pattern  : debe cumplir la expresión regular.
 */
@Getter
@Setter
@NoArgsConstructor
public class RegistroDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 60, message = "El apellido no puede superar los 60 caracteres.")
    private String apellido;

    @NotBlank(message = "El documento es obligatorio.")
    @Pattern(regexp = "^[A-Za-z0-9]{6,20}$",
            message = "El documento debe tener entre 6 y 20 caracteres alfanuméricos, sin espacios ni puntos.")
    private String documento;

    /**
     * @DateTimeFormat(iso = DATE): le indica a Spring como convertir el texto
     * "yyyy-MM-dd" que envía <input type="date"> a LocalDate (y al reves al
     * mostrar el formulario).
     */
    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo personal es obligatorio.")
    @Email(message = "Ingrese un correo electrónico válido.")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres.")
    private String correo;

    /**
     * El máximo de 32 caracteres evita superar el limite de 72 BYTES que admite
     * el algoritmo BCrypt con el que se guarda la clave.
     */
    @NotBlank(message = "La clave es obligatoria.")
    @Size(min = 6, max = 32, message = "La clave debe tener entre 6 y 32 caracteres.")
    private String clave;

    @NotBlank(message = "Debe repetir la clave.")
    private String confirmarClave;
}
