package com.colegio.sistemaescolar.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO de Materia. */
@Data
public class MateriaDTO {

    private long id;

    @NotBlank(message = "El nombre de la materia es obligatorio.")
    @Size(max = 80, message = "Máximo 80 caracteres.")
    private String nombre;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String descripcion;
}
