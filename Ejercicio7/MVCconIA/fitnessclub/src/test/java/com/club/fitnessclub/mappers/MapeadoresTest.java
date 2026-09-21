package com.club.fitnessclub.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.club.fitnessclub.DatosDePrueba;
import com.club.fitnessclub.dtos.CuotaDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.enums.EstadoCuota;

/** PRUEBAS UNITARIAS de los mapeadores (conversión entidad <-> DTO). No requieren Mockito ni Spring. */
class MapeadoresTest {

    private final MapeadorCuota mapeadorCuota = new MapeadorCuota(DatosDePrueba.RELOJ, new MapeadorPago());

    @Test
    @DisplayName("Duración: formatea horas y minutos y nunca es negativa")
    void formatearDuracion() {
        assertThat(MapeadorRegistroAcceso.formatearDuracion(Duration.ofMinutes(45))).isEqualTo("45 min");
        assertThat(MapeadorRegistroAcceso.formatearDuracion(Duration.ofMinutes(80))).isEqualTo("1 h 20 min");
        assertThat(MapeadorRegistroAcceso.formatearDuracion(Duration.ofMinutes(-5))).isEqualTo("0 min");
    }

    @Test
    @DisplayName("Período: texto en español con la primera letra en mayúscula")
    void textoPeriodo() {
        assertThat(MapeadorCuota.textoPeriodo(LocalDate.of(2026, 9, 1))).isEqualTo("Septiembre 2026");
    }

    @Test
    @DisplayName("DTO -> entidad: el período YearMonth se guarda como primer día del mes")
    void aEntidad_normalizaPeriodo() {
        CuotaDTO dto = new CuotaDTO();
        dto.setPeriodo(YearMonth.of(2026, 3));
        dto.setImporte(new BigDecimal("100.00"));
        dto.setFechaVencimiento(LocalDate.of(2026, 3, 10));

        Cuota cuota = mapeadorCuota.aEntidad(dto);

        assertThat(cuota.getPeriodo()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PENDIENTE);
    }

    @Test
    @DisplayName("Entidad -> DTO: una cuota pendiente con vencimiento pasado figura como vencida")
    void aDTO_cuotaVencida() {
        GrupoFamiliar grupo = DatosDePrueba.grupo(1L, "Familia Pérez");
        Cuota cuota = DatosDePrueba.cuota(1L, grupo, "1000.00", LocalDate.of(2026, 8, 1)); // vence 10/08, hoy 15/09

        assertThat(mapeadorCuota.aDTO(cuota).isVencida()).isTrue();

        cuota.setEstado(EstadoCuota.PAGADA);
        assertThat(mapeadorCuota.aDTO(cuota).isVencida()).isFalse(); // una cuota pagada nunca está vencida
    }
}
