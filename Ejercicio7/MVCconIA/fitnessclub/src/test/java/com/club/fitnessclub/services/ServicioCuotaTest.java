package com.club.fitnessclub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.club.fitnessclub.DatosDePrueba;
import com.club.fitnessclub.dtos.CuotaDTO;
import com.club.fitnessclub.dtos.GeneracionCuotasDTO;
import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.MedioPago;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorCuota;
import com.club.fitnessclub.mappers.MapeadorPago;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;

/** PRUEBAS UNITARIAS de ServicioCuota (emisión, modificación y anulación de cuotas). */
@ExtendWith(MockitoExtension.class)
class ServicioCuotaTest {

    private static final LocalDate SEPTIEMBRE = LocalDate.of(2026, 9, 1);

    @Mock private RepositorioCuota repositorioCuota;
    @Mock private RepositorioGrupoFamiliar repositorioGrupo;

    private ServicioCuota servicio;
    private GrupoFamiliar grupo;

    @BeforeEach
    void preparar() {
        servicio = new ServicioCuota(repositorioCuota, repositorioGrupo,
                new MapeadorCuota(DatosDePrueba.RELOJ, new MapeadorPago()));
        grupo = DatosDePrueba.grupo(1L, "Familia Pérez");
    }

    private CuotaDTO dto(LocalDate vencimiento) {
        CuotaDTO dto = new CuotaDTO();
        dto.setGrupoFamiliarId(1L);
        dto.setPeriodo(YearMonth.of(2026, 9));
        dto.setImporte(new BigDecimal("25000.00"));
        dto.setFechaVencimiento(vencimiento);
        return dto;
    }

    @Test
    @DisplayName("Emisión: crea la cuota PENDIENTE con el período normalizado al primer día del mes")
    void save_ok() throws Exception {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioCuota.save(any(Cuota.class))).thenAnswer(inv -> inv.getArgument(0));

        CuotaDTO resultado = servicio.save(dto(LocalDate.of(2026, 9, 20)));

        assertThat(resultado.getEstado()).isEqualTo(EstadoCuota.PENDIENTE);
        assertThat(resultado.getPeriodo()).isEqualTo(YearMonth.of(2026, 9));
        assertThat(resultado.getPeriodoTexto()).isEqualTo("Septiembre 2026");
        assertThat(resultado.getSaldo()).isEqualByComparingTo("25000.00");
        assertThat(resultado.isVencida()).isFalse(); // vence el 20/09 y "hoy" es 15/09
    }

    @Test
    @DisplayName("Emisión: una familia no puede tener dos cuotas del mismo período")
    void save_periodoDuplicado_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioCuota.existsByGrupoFamiliarIdAndPeriodo(1L, SEPTIEMBRE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.save(dto(LocalDate.of(2026, 9, 10))))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("ya tiene una cuota");
        verify(repositorioCuota, never()).save(any());
    }

    @Test
    @DisplayName("Emisión: el vencimiento no puede ser anterior al período")
    void save_vencimientoAnteriorAlPeriodo_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> servicio.save(dto(LocalDate.of(2026, 8, 31))))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("vencimiento");
    }

    @Test
    @DisplayName("Emisión masiva: omite las familias que ya tienen la cuota y cuenta las creadas")
    void generarParaTodas_omiteExistentes() throws Exception {
        GrupoFamiliar otra = DatosDePrueba.grupo(2L, "Familia Gómez");
        when(repositorioGrupo.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(grupo, otra));
        when(repositorioCuota.existsByGrupoFamiliarIdAndPeriodo(1L, SEPTIEMBRE)).thenReturn(true);
        when(repositorioCuota.existsByGrupoFamiliarIdAndPeriodo(2L, SEPTIEMBRE)).thenReturn(false);

        GeneracionCuotasDTO gen = new GeneracionCuotasDTO();
        gen.setPeriodo(YearMonth.of(2026, 9));
        gen.setImporte(new BigDecimal("25000"));
        gen.setFechaVencimiento(LocalDate.of(2026, 9, 10));

        int creadas = servicio.generarParaTodas(gen);

        assertThat(creadas).isEqualTo(1);
        verify(repositorioCuota, times(1)).save(any(Cuota.class));
    }

    @Test
    @DisplayName("Anulación: no se puede anular una cuota con pagos vigentes")
    void delete_conPagos_lanzaExcepcion() {
        Cuota cuota = DatosDePrueba.cuota(1L, grupo, "1000.00", SEPTIEMBRE);
        DatosDePrueba.pago(1L, cuota, "300.00", MedioPago.EFECTIVO);
        when(repositorioCuota.findByIdConDetalle(1L)).thenReturn(Optional.of(cuota));

        assertThatThrownBy(() -> servicio.delete(1L))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("pagos registrados");
        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.PENDIENTE);
    }

    @Test
    @DisplayName("Anulación: una cuota sin pagos pasa a ANULADA")
    void delete_sinPagos_seAnula() throws Exception {
        Cuota cuota = DatosDePrueba.cuota(1L, grupo, "1000.00", SEPTIEMBRE);
        when(repositorioCuota.findByIdConDetalle(1L)).thenReturn(Optional.of(cuota));

        servicio.delete(1L);

        assertThat(cuota.getEstado()).isEqualTo(EstadoCuota.ANULADA);
        verify(repositorioCuota).save(cuota);
    }

    @Test
    @DisplayName("Modificación: el importe no puede quedar por debajo de lo ya pagado")
    void update_importeMenorAlPagado_lanzaExcepcion() {
        Cuota cuota = DatosDePrueba.cuota(1L, grupo, "1000.00", SEPTIEMBRE);
        DatosDePrueba.pago(1L, cuota, "800.00", MedioPago.EFECTIVO);
        when(repositorioCuota.findByIdConDetalle(1L)).thenReturn(Optional.of(cuota));

        CuotaDTO cambio = dto(LocalDate.of(2026, 9, 10));
        cambio.setImporte(new BigDecimal("500.00"));

        assertThatThrownBy(() -> servicio.update(1L, cambio))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("menor a lo ya pagado");
    }
}
