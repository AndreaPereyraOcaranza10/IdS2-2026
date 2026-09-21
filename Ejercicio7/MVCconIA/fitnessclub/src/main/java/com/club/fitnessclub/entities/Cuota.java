package com.club.fitnessclub.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.club.fitnessclub.enums.EstadoCuota;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuota mensual del club emitida a UNA FAMILIA.
 *
 * <p>Restricción de unicidad (grupo_familiar_id, periodo): una familia no puede
 * tener dos cuotas del mismo mes.
 *
 * <p>"periodo" guarda el PRIMER DÍA del mes (p. ej. 2026-09-01). Se usa LocalDate
 * porque Hibernate no mapea YearMonth de forma nativa; el mapeador convierte a YearMonth
 * para la vista.
 *
 * <p>Una cuota puede saldarse con uno o varios Pago (p. ej. mitad efectivo y mitad
 * transferencia). {@link #actualizarEstado()} la pasa a PAGADA cuando lo pagado alcanza el importe.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cuota", uniqueConstraints = @UniqueConstraint(name = "uk_cuota_grupo_periodo", columnNames = { "grupo_familiar_id", "periodo" }))
public class Cuota extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_familiar_id", nullable = false)
    private GrupoFamiliar grupoFamiliar;

    /** Primer día del mes que se cobra. */
    @Column(nullable = false)
    private LocalDate periodo;

    /** Dinero siempre con BigDecimal (double genera errores de redondeo). precision 12, 2 decimales. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    /** Pagos aplicados a esta cuota. cascade ALL: registrar un pago desde la cuota lo persiste. */
    @OneToMany(mappedBy = "cuota", cascade = CascadeType.ALL)
    private List<Pago> pagos = new ArrayList<>();

    /** Suma de los pagos activos. Recorre una colección LAZY: solo invocar dentro de una transacción. */
    public BigDecimal getMontoPagado() {
        return pagos.stream()
                .filter(Pago::isActivo)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lo que falta abonar (nunca negativo). */
    public BigDecimal getSaldo() {
        BigDecimal saldo = importe.subtract(getMontoPagado());
        return saldo.signum() < 0 ? BigDecimal.ZERO : saldo;
    }

    /** Mantiene sincronizados ambos lados de la relación cuota <-> pago. */
    public void agregarPago(Pago pago) {
        pagos.add(pago);
        pago.setCuota(this);
    }

    /**
     * Recalcula el estado según lo pagado. Una cuota ANULADA no cambia de estado.
     * Se invoca al registrar o anular un pago.
     */
    public void actualizarEstado() {
        if (estado == EstadoCuota.ANULADA) {
            return;
        }
        estado = getSaldo().signum() == 0 ? EstadoCuota.PAGADA : EstadoCuota.PENDIENTE;
    }
}
