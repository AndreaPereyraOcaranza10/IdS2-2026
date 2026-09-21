package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.enums.Periodo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO de Nota. Escala 1 a 10 (hasta dos decimales). */
@Data
public class NotaDTO {

    private long id;

    @NotNull(message = "Seleccione el alumno.")
    private Long alumnoId;

    @NotNull(message = "Seleccione la materia.")
    private Long materiaId;

    @NotNull(message = "Seleccione el período.")
    private Periodo periodo;

    @NotNull(message = "Ingrese la nota.")
    @DecimalMin(value = "1.00", message = "La nota mínima es 1.")
    @DecimalMax(value = "10.00", message = "La nota máxima es 10.")
    @Digits(integer = 2, fraction = 2, message = "Use hasta dos decimales.")
    private BigDecimal valor;

    @NotNull(message = "La fecha es obligatoria.")
    @PastOrPresent(message = "La fecha no puede ser futura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String observaciones;

    // ----- Campos de solo lectura (para listas y detalle) -----
    private String alumnoNombre;
    private String gradoNombre;
    private String materiaNombre;
    private String periodoEtiqueta;
    private boolean aprobada;
    private String cargadaPor;
}
