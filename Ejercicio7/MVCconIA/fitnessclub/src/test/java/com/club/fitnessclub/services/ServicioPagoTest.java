package com.club.fitnessclub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.club.fitnessclub.DatosDePrueba;
import com.club.fitnessclub.dtos.PagoDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Pago;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.MedioPago;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorPago;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioPago;

/** PRUEBAS UNITARIAS de ServicioPago: cobro de la cuota familiar con distintos medios de pago. */
@ExtendWith(MockitoExtension.class)
class ServicioPagoTest {

    @Mock private RepositorioPago repositorioPago;
    @Mock private RepositorioCuota repositorioCuota;

    private ServicioPago servicio;
    private Cuota cuota;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPago(repositorioPago, repositorioCuota, new MapeadorPago(), DatosDePrueba.RELOJ);
        GrupoFamiliar grupo = DatosDePrueba.grupo(1L, "Familia Pérez");
        cuota = DatosDePrueba.cuota(1L, grupo, "1000.00", LocalDate.of(2026, 9, 1));
    }

    private PagoDTO dto(String monto, MedioPago medio, String referencia) {
        PagoDTO dto = new PagoDTO();
        dto.setCuotaId(1L);
        dto.setMonto(new BigDecimal(monto));
        dto.setMedioPago(medio);
        dto.setReferencia(referencia);
        return dto;
    }

    private void cuotaEncontrada() {
        when(repositorioCuota.findByIdConDetalle(1L)).thenReturn(Optional.of(cuota));
    }

    @Test
    @DisplayName("Pago total en efectivo: la cuota pasa a PAGADA y la fecha la fija el servidor")
    void registrar_pagoTotal_cuotaPagada() throws Exception {
        cuotaEncontrada();
        when(repositorioPago.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoDTO resultado = servicio.registrar(dto("1000.00", MedioPago.EFECTIVO, null));

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PAGADA);
        assertThat(cuota.getSaldo()).isEqualByComparingTo("0");
        assertThat(resultado.getFechaPago()).isEqualTo(LocalDateTime.of(2026, 9, 15, 12, 0));
        assertThat(resultado.getMedioPago()).isEqualTo(MedioPago.EFECTIVO);
        assertThat(resultado.getReferencia()).isNull();
    }

    @Test
    @DisplayName("Pago parcial por transferencia: la cuota sigue PENDIENTE con el saldo restante")
    void registrar_pagoParcial_sigueSaldoPendiente() throws Exception {
        cuotaEncontrada();
        when(repositorioPago.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoDTO resultado = servicio.registrar(dto("400.00", MedioPago.TRANSFERENCIA, "OP-123"));

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PENDIENTE);
        assertThat(cuota.getSaldo()).isEqualByComparingTo("600.00");
        assertThat(resultado.getReferencia()).isEqualTo("OP-123");
    }

    @Test
    @DisplayName("Pagos combinados (dos medios) que suman el importe cancelan la cuota")
    void registrar_dosPagosCombinados_cuotaPagada() throws Exception {
        cuotaEncontrada();
        when(repositorioPago.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.registrar(dto("400.00", MedioPago.EFECTIVO, null));
        servicio.registrar(dto("600.00", MedioPago.MERCADO_PAGO, "MP-987"));

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PAGADA);
        assertThat(cuota.getPagos()).hasSize(2);
    }

    @Test
    @DisplayName("No se admite un monto mayor al saldo pendiente")
    void registrar_superaSaldo_lanzaExcepcion() {
        cuotaEncontrada();

        assertThatThrownBy(() -> servicio.registrar(dto("1000.01", MedioPago.EFECTIVO, null)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("supera el saldo");
        verify(repositorioPago, never()).save(any());
        assertThat(cuota.getPagos()).isEmpty();
    }

    @Test
    @DisplayName("Transferencia y Mercado Pago exigen comprobante")
    void registrar_medioElectronicoSinReferencia_lanzaExcepcion() {
        cuotaEncontrada();

        assertThatThrownBy(() -> servicio.registrar(dto("500.00", MedioPago.MERCADO_PAGO, "  ")))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("comprobante");
    }

    @Test
    @DisplayName("No se puede pagar una cuota anulada")
    void registrar_cuotaAnulada_lanzaExcepcion() {
        cuota.setEstado(EstadoCuota.ANULADA);
        cuotaEncontrada();

        assertThatThrownBy(() -> servicio.registrar(dto("100.00", MedioPago.EFECTIVO, null)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("anulada");
    }

    @Test
    @DisplayName("No se puede pagar una cuota ya pagada")
    void registrar_cuotaYaPagada_lanzaExcepcion() {
        cuota.setEstado(EstadoCuota.PAGADA);
        cuotaEncontrada();

        assertThatThrownBy(() -> servicio.registrar(dto("100.00", MedioPago.EFECTIVO, null)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("totalmente pagada");
    }

    @Test
    @DisplayName("Una cuota inexistente se rechaza")
    void registrar_cuotaInexistente_lanzaExcepcion() {
        when(repositorioCuota.findByIdConDetalle(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrar(dto("100.00", MedioPago.EFECTIVO, null)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Anular un pago: queda inactivo y la cuota vuelve a PENDIENTE")
    void anular_pagoDeCuotaPagada_cuotaVuelveAPendiente() throws Exception {
        Pago pago = DatosDePrueba.pago(9L, cuota, "1000.00", MedioPago.EFECTIVO);
        cuota.actualizarEstado();
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PAGADA); // precondición
        when(repositorioPago.findByIdAndActivoTrue(9L)).thenReturn(Optional.of(pago));

        servicio.anular(9L);

        assertThat(pago.isActivo()).isFalse();
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PENDIENTE);
        assertThat(cuota.getSaldo()).isEqualByComparingTo("1000.00");
        verify(repositorioPago).save(pago);
    }

    @Test
    @DisplayName("Anular un pago inexistente o ya anulado se rechaza")
    void anular_pagoInexistente_lanzaExcepcion() {
        when(repositorioPago.findByIdAndActivoTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.anular(9L)).isInstanceOf(ExcepcionNegocio.class);
    }
}
