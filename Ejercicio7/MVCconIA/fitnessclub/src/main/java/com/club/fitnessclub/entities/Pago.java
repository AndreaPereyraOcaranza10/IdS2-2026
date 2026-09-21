package com.club.fitnessclub.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.club.fitnessclub.enums.MedioPago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pago aplicado a una cuota.
 *
 * <p>El medio se modela con el enum MedioPago (EFECTIVO, TRANSFERENCIA, MERCADO_PAGO)
 * y un campo "referencia":
 * <ul>
 *   <li>EFECTIVO: nro. de recibo (opcional).</li>
 *   <li>TRANSFERENCIA: nro. de operación/comprobante (obligatorio).</li>
 *   <li>MERCADO_PAGO: ID de la operación de Mercado Pago (obligatorio).</li>
 * </ul>
 * Los tres medios comparten los mismos atributos, por eso NO se usa herencia de
 * entidades. La integración real con la API de Mercado Pago queda fuera del alcance:
 * se registra el ID de la operación que informa el cliente.
 *
 * <p>Los pagos son INMUTABLES: no se modifican, solo se anulan (baja lógica, activo=false).
 * La auditoría deja registrado qué usuario cargó el pago (creado_por) y quién lo anuló (modificado_por).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pago")
public class Pago extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuota_id", nullable = false)
    private Cuota cuota;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Momento en que se recibió el pago. */
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 20)
    private MedioPago medioPago;

    /** Comprobante / ID de operación según el medio (ver documentación de la clase). */
    @Column(length = 80)
    private String referencia;
}
