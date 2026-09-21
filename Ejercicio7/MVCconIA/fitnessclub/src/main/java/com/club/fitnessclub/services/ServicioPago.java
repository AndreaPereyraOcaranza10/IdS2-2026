package com.club.fitnessclub.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.dtos.PagoDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.Pago;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.MedioPago;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorPago;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioPago;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de PAGOS de cuotas: registra el pago de la cuota de una familia con distintos medios
 * (Efectivo, Transferencia, Mercado Pago).
 *
 * <p>No implementa ServicioBase: los pagos son INMUTABLES (no se editan). Se registran y, si hubo
 * un error de carga, se ANULAN (baja lógica); la auditoría deja quién lo cargó y quién lo anuló.
 *
 * <p>REGLAS DE NEGOCIO:
 * <ol>
 *   <li>Solo se paga una cuota PENDIENTE (no anulada ni ya pagada).</li>
 *   <li>El monto debe ser mayor a cero y NO puede superar el saldo pendiente (no hay sobrepagos).</li>
 *   <li>Transferencia y Mercado Pago exigen un comprobante / ID de operación (Efectivo: opcional).</li>
 *   <li>La fecha del pago la fija el servidor (Clock); el usuario no puede falsearla.</li>
 *   <li>Cuando el total pagado alcanza el importe, la cuota pasa automáticamente a PAGADA;
 *       al anular un pago, vuelve a PENDIENTE.</li>
 * </ol>
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServicioPago {

    private final RepositorioPago repositorioPago;
    private final RepositorioCuota repositorioCuota;
    private final MapeadorPago mapeador;
    private final Clock reloj;

    /** Historial de pagos vigentes (del más reciente al más antiguo). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<PagoDTO> findAll() throws Exception {
        return repositorioPago.findByActivoTrueOrderByFechaPagoDesc().stream()
                .map(mapeador::aDTO)
                .toList();
    }

    /** Registra un pago sobre una cuota (reglas 1 a 5). */
    public PagoDTO registrar(PagoDTO dto) throws Exception {
        Cuota cuota = repositorioCuota.findByIdConDetalle(dto.getCuotaId())
                .orElseThrow(() -> new ExcepcionNegocio("La cuota indicada no existe."));

        // Regla 1
        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new ExcepcionNegocio("La cuota está anulada: no admite pagos.");
        }
        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            throw new ExcepcionNegocio("La cuota ya está totalmente pagada.");
        }

        // Regla 2
        BigDecimal monto = dto.getMonto();
        if (monto == null || monto.signum() <= 0) {
            throw new ExcepcionNegocio("El monto debe ser mayor a cero.");
        }
        BigDecimal saldo = cuota.getSaldo();
        if (monto.compareTo(saldo) > 0) {
            throw new ExcepcionNegocio("El monto ($" + monto + ") supera el saldo pendiente de la cuota ($" + saldo + ").");
        }

        // Regla 3
        MedioPago medio = dto.getMedioPago();
        String referencia = dto.getReferencia() == null ? "" : dto.getReferencia().trim();
        if (medio.requiereReferencia() && referencia.isEmpty()) {
            throw new ExcepcionNegocio("Para pagos por " + medio.getEtiqueta()
                    + " debe informar el número de comprobante / operación.");
        }

        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setMedioPago(medio);
        pago.setReferencia(referencia.isEmpty() ? null : referencia);
        pago.setFechaPago(LocalDateTime.now(reloj)); // Regla 4
        cuota.agregarPago(pago);

        // Regla 5: la cuota pasa a PAGADA si el saldo quedó en cero.
        cuota.actualizarEstado();

        // Se guarda el pago (y no la cuota) para que la instancia devuelta sea la persistida
        // con su id; la cuota está gestionada y su nuevo estado se confirma con la transacción.
        return mapeador.aDTO(repositorioPago.save(pago));
    }

    /** Anula un pago (baja lógica) y recalcula el estado de su cuota. */
    public void anular(long pagoId) throws Exception {
        Pago pago = repositorioPago.findByIdAndActivoTrue(pagoId)
                .orElseThrow(() -> new ExcepcionNegocio("El pago solicitado no existe o ya fue anulado."));

        pago.setActivo(false);
        pago.getCuota().actualizarEstado(); // el pago anulado ya no suma en getMontoPagado()
        repositorioPago.save(pago);
    }
}
