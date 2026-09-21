package com.club.fitnessclub.mappers;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.CuotaDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.Pago;
import com.club.fitnessclub.enums.EstadoCuota;

import lombok.RequiredArgsConstructor;

/**
 * Mapeador Cuota (entidad) <-> CuotaDTO.
 *
 * <p>Convierte el período: la entidad guarda el primer día del mes (LocalDate) y el DTO usa
 * YearMonth. Calcula los campos derivados (montoPagado, saldo, vencida) usando el Clock inyectado.
 * aDTO() recorre colecciones LAZY (pagos) y la relación grupoFamiliar: ejecutar dentro de una
 * transacción con los datos cargados (ver @Query con JOIN FETCH en RepositorioCuota).
 */
@Component
@RequiredArgsConstructor
public class MapeadorCuota {

    private static final DateTimeFormatter FORMATO_PERIODO =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-AR"));

    private final Clock reloj;
    private final MapeadorPago mapeadorPago;

    /** Entidad -> DTO sin la lista de pagos (para listados). */
    public CuotaDTO aDTO(Cuota cuota) {
        CuotaDTO dto = new CuotaDTO();
        dto.setId(cuota.getId());
        dto.setGrupoFamiliarId(cuota.getGrupoFamiliar().getId());
        dto.setGrupoFamiliarNombre(cuota.getGrupoFamiliar().getNombre());
        dto.setPeriodo(YearMonth.from(cuota.getPeriodo()));
        dto.setPeriodoTexto(textoPeriodo(cuota.getPeriodo()));
        dto.setImporte(cuota.getImporte());
        dto.setFechaVencimiento(cuota.getFechaVencimiento());
        dto.setEstado(cuota.getEstado());
        dto.setMontoPagado(cuota.getMontoPagado());
        dto.setSaldo(cuota.getSaldo());
        dto.setVencida(cuota.getEstado() == EstadoCuota.PENDIENTE
                && cuota.getFechaVencimiento().isBefore(LocalDate.now(reloj)));
        return dto;
    }

    /** Entidad -> DTO incluyendo los pagos vigentes (para la pantalla de detalle). */
    public CuotaDTO aDTOConPagos(Cuota cuota) {
        CuotaDTO dto = aDTO(cuota);
        cuota.getPagos().stream()
                .filter(Pago::isActivo)
                .map(mapeadorPago::aDTO)
                .forEach(dto.getPagos()::add);
        return dto;
    }

    /** DTO -> entidad NUEVA. La familia la asigna el servicio; el estado inicial es PENDIENTE. */
    public Cuota aEntidad(CuotaDTO dto) {
        Cuota cuota = new Cuota();
        cuota.setPeriodo(dto.getPeriodo().atDay(1));
        cuota.setImporte(dto.getImporte());
        cuota.setFechaVencimiento(dto.getFechaVencimiento());
        cuota.setEstado(EstadoCuota.PENDIENTE);
        return cuota;
    }

    /** Modificación: solo importe y vencimiento (el período y la familia no se cambian). */
    public void actualizarEntidad(Cuota cuota, CuotaDTO dto) {
        cuota.setImporte(dto.getImporte());
        cuota.setFechaVencimiento(dto.getFechaVencimiento());
    }

    /** 2026-09-01 -> "Septiembre 2026". */
    public static String textoPeriodo(LocalDate periodo) {
        String texto = FORMATO_PERIODO.format(periodo);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
