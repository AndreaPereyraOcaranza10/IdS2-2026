package com.club.fitnessclub.mappers;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.PagoDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.Pago;

/**
 * Mapeador Pago (entidad) <-> PagoDTO.
 *
 * <p>aDTO() usa pago.getCuota().getGrupoFamiliar() (ambos LAZY): los repositorios que devuelven
 * pagos para listados usan @EntityGraph, o se invoca dentro de la transacción del servicio.
 * La entidad se crea en ServicioPago (necesita la fecha del reloj y la cuota), por eso este
 * mapeador no tiene aEntidad().
 */
@Component
public class MapeadorPago {

    public PagoDTO aDTO(Pago pago) {
        Cuota cuota = pago.getCuota();

        PagoDTO dto = new PagoDTO();
        dto.setId(pago.getId());
        dto.setCuotaId(cuota.getId());
        dto.setMonto(pago.getMonto());
        dto.setMedioPago(pago.getMedioPago());
        dto.setReferencia(pago.getReferencia());
        dto.setFechaPago(pago.getFechaPago());
        dto.setGrupoFamiliarNombre(cuota.getGrupoFamiliar().getNombre());
        dto.setPeriodoTexto(MapeadorCuota.textoPeriodo(cuota.getPeriodo()));
        dto.setRegistradoPor(pago.getCreadoPor());
        dto.setActivo(pago.isActivo());
        return dto;
    }
}
