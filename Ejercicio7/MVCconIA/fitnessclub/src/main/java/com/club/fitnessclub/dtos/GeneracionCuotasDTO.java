package com.club.fitnessclub.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO del formulario "Generar cuotas del mes": emite la cuota a TODAS las familias vigentes. */
@Data
@NoArgsConstructor
public class GeneracionCuotasDTO {

    @NotNull(message = "Debe indicar el período (mes y año)")
    private YearMonth periodo;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "Importe inválido (hasta 10 enteros y 2 decimales)")
    private BigDecimal importe;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimiento;
}
