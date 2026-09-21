package com.colegio.sistemaescolar.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO (Data Transfer Object) de Grado.
 *
 * <p>Un DTO es un objeto simple que viaja entre capas (controlador <-> servicio <-> vista) en lugar de
 * la entidad JPA. Ventajas: la vista no depende del modelo de base de datos, no hay LazyInitializationException,
 * y se controla exactamente qué datos se exponen y validan. Las anotaciones {@code @NotBlank}, {@code @Size},
 * etc. (Jakarta Validation) se comprueban cuando el controlador usa {@code @Valid}.</p>
 *
 * <p>{@code @Data} (Lombok) genera getters, setters, equals, hashCode y toString.</p>
 */
@Data
public class GradoDTO {

    /** 0 = registro nuevo. */
    private long id;

    @NotBlank(message = "El nombre del grado es obligatorio.")
    @Size(max = 60, message = "Máximo 60 caracteres.")
    private String nombre;

    @NotNull(message = "Indique el nivel (número de grado).")
    @Min(value = 1, message = "El nivel mínimo es 1.")
    @Max(value = 12, message = "El nivel máximo es 12.")
    private Integer nivel;

    /** Solo lectura: alumnos activos del grado (se calcula en el servicio). */
    private long cantidadAlumnos;
}
