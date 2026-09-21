package com.colegio.sistemaescolar.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO de Aula (ver {@link GradoDTO} para la explicación del patrón DTO). */
@Data
public class AulaDTO {

    private long id;

    @NotBlank(message = "El código del aula es obligatorio.")
    @Size(max = 20, message = "Máximo 20 caracteres.")
    private String codigo;

    @NotNull(message = "Indique la capacidad.")
    @Min(value = 1, message = "La capacidad mínima es 1.")
    @Max(value = 100, message = "La capacidad máxima es 100.")
    private Integer capacidad;

    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String ubicacion;

    /** Solo lectura: alumnos activos en el aula. */
    private long ocupacion;
}
