package com.club.fitnessclub.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.club.fitnessclub.enums.EstadoCuota;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Cuota. El período viaja como YearMonth (año-mes): un input type="month" envía "2026-09"
 * y Spring lo convierte automáticamente. El mapeador lo traduce al primer día del mes de la entidad.
 */
@Data
@NoArgsConstructor
public class CuotaDTO {

    private long id;

    @NotNull(message = "Debe seleccionar una familia")
    private Long grupoFamiliarId;

    @NotNull(message = "Debe indicar el período (mes y año)")
    private YearMonth periodo;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "Importe inválido (hasta 10 enteros y 2 decimales)")
    private BigDecimal importe;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaVencimiento;

    // ----- Solo lectura (calculados por el mapeador) -----

    private String grupoFamiliarNombre;
    /** "Septiembre 2026". */
    private String periodoTexto;
    private EstadoCuota estado = EstadoCuota.PENDIENTE;
    private BigDecimal montoPagado = BigDecimal.ZERO;
    private BigDecimal saldo = BigDecimal.ZERO;
    /** PENDIENTE con fecha de vencimiento anterior a hoy. */
    private boolean vencida;
    /** Pagos activos de la cuota (se completa en el detalle). */
    private List<PagoDTO> pagos = new ArrayList<>();
}
