package com.club.fitnessclub.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.club.fitnessclub.enums.MedioPago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Pago. Entrada: cuotaId, monto, medioPago, referencia. La fecha del pago NO la elige el
 * usuario: la fija el servicio con el reloj del sistema (evita pagos con fecha falsificada).
 */
@Data
@NoArgsConstructor
public class PagoDTO {

    private long id;

    @NotNull(message = "Falta indicar la cuota")
    private Long cuotaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "Monto inválido (hasta 10 enteros y 2 decimales)")
    private BigDecimal monto;

    @NotNull(message = "Seleccioná el medio de pago")
    private MedioPago medioPago;

    @Size(max = 80, message = "La referencia no puede superar los 80 caracteres")
    private String referencia;

    // ----- Solo lectura -----

    private LocalDateTime fechaPago;
    private String grupoFamiliarNombre;
    private String periodoTexto;
    /** Usuario que registró el pago (dato de auditoría). */
    private String registradoPor;
    private boolean activo = true;
}
