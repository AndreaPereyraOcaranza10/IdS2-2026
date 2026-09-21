package com.club.fitnessclub.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) de GrupoFamiliar: es lo único que ven el controlador y la vista.
 *
 * <ul>
 *   <li>Los campos de ENTRADA (nombre, direccion, telefono) llevan validaciones de Bean
 *       Validation; se activan con @Valid en el controlador.</li>
 *   <li>Los campos de SOLO LECTURA (titularNombre, cantidadIntegrantes, activo) los calcula el
 *       mapeador para los listados; el formulario los ignora.</li>
 * </ul>
 *
 * <p>@Data genera getters/setters/toString/equals: es seguro en un DTO porque no tiene
 * relaciones LAZY (a diferencia de las entidades).
 */
@Data
@NoArgsConstructor
public class GrupoFamiliarDTO {

    /** 0 = registro nuevo (todavía sin id asignado). */
    private long id;

    @NotBlank(message = "El nombre de la familia es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    private String direccion;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    // ----- Solo lectura (calculados por el mapeador) -----

    /** "Apellido, Nombre" del titular; null si la familia aún no tiene titular. */
    private String titularNombre;

    private int cantidadIntegrantes;

    private boolean activo = true;
}
