package com.club.fitnessclub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.club.fitnessclub.DatosDePrueba;
import com.club.fitnessclub.dtos.RegistroAccesoDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.entities.RegistroAcceso;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorRegistroAcceso;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioPersona;
import com.club.fitnessclub.repositories.RepositorioRegistroAcceso;

/** PRUEBAS UNITARIAS de ServicioAcceso (entradas y salidas). Usa un reloj fijo: "ahora" = 2026-09-15 12:00. */
@ExtendWith(MockitoExtension.class)
class ServicioAccesoTest {

    private static final String DNI = "30123456";
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 15, 12, 0);

    @Mock private RepositorioRegistroAcceso repositorioAcceso;
    @Mock private RepositorioPersona repositorioPersona;
    @Mock private RepositorioCuota repositorioCuota;

    private ServicioAcceso servicio;
    private GrupoFamiliar grupo;
    private Persona persona;

    @BeforeEach
    void preparar() {
        servicio = new ServicioAcceso(repositorioAcceso, repositorioPersona, repositorioCuota,
                new MapeadorRegistroAcceso(DatosDePrueba.RELOJ), DatosDePrueba.RELOJ);
        grupo = DatosDePrueba.grupo(1L, "Familia Pérez");
        persona = DatosDePrueba.persona(5L, DNI, grupo, Parentesco.TITULAR);
    }

    @Test
    @DisplayName("Entrada: registra la hora del reloj del servidor y deja a la persona adentro")
    void registrarEntrada_ok() throws Exception {
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.of(persona));
        when(repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(5L)).thenReturn(Optional.empty());
        when(repositorioAcceso.save(any(RegistroAcceso.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroAccesoDTO dto = servicio.registrarEntrada(DNI);

        assertThat(dto.getFechaHoraEntrada()).isEqualTo(AHORA);
        assertThat(dto.isAdentro()).isTrue();
        assertThat(dto.getPersonaDni()).isEqualTo(DNI);
        assertThat(dto.getAdvertencia()).isNull();
    }

    @Test
    @DisplayName("Entrada: no se permite si la persona ya tiene una visita abierta")
    void registrarEntrada_yaAdentro_lanzaExcepcion() {
        RegistroAcceso abierto = new RegistroAcceso();
        abierto.setPersona(persona);
        abierto.setFechaHoraEntrada(AHORA.minusHours(1));
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.of(persona));
        when(repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(5L)).thenReturn(Optional.of(abierto));

        assertThatThrownBy(() -> servicio.registrarEntrada(DNI))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("ya tiene una entrada registrada");
        org.mockito.Mockito.verify(repositorioAcceso, never()).save(any());
    }

    @Test
    @DisplayName("Entrada: si la familia tiene cuotas vencidas se agrega una advertencia (no bloquea)")
    void registrarEntrada_conDeuda_agregaAdvertencia() throws Exception {
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.of(persona));
        when(repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(5L)).thenReturn(Optional.empty());
        when(repositorioAcceso.save(any(RegistroAcceso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repositorioCuota.existsByGrupoFamiliarIdAndEstadoAndFechaVencimientoBeforeAndActivoTrue(
                eq(1L), eq(EstadoCuota.PENDIENTE), eq(DatosDePrueba.HOY))).thenReturn(true);

        RegistroAccesoDTO dto = servicio.registrarEntrada(DNI);

        assertThat(dto.isAdentro()).isTrue(); // el ingreso SE registró igual
        assertThat(dto.getAdvertencia()).contains("cuotas vencidas");
    }

    @Test
    @DisplayName("Entrada: un DNI con formato inválido se rechaza sin consultar la base")
    void registrarEntrada_dniInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> servicio.registrarEntrada("12ab"))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("DNI válido");
        verifyNoInteractions(repositorioPersona, repositorioAcceso);
    }

    @Test
    @DisplayName("Entrada: una persona inexistente o dada de baja se rechaza")
    void registrarEntrada_personaInexistente_lanzaExcepcion() {
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrarEntrada(DNI))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("No hay una persona vigente");
    }

    @Test
    @DisplayName("Salida: cierra la visita abierta y calcula la permanencia")
    void registrarSalida_ok() throws Exception {
        RegistroAcceso abierto = new RegistroAcceso();
        abierto.setPersona(persona);
        abierto.setFechaHoraEntrada(AHORA.minusMinutes(90));
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.of(persona));
        when(repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(5L)).thenReturn(Optional.of(abierto));
        when(repositorioAcceso.save(any(RegistroAcceso.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistroAccesoDTO dto = servicio.registrarSalida(DNI);

        assertThat(dto.getFechaHoraSalida()).isEqualTo(AHORA);
        assertThat(dto.isAdentro()).isFalse();
        assertThat(dto.getPermanencia()).isEqualTo("1 h 30 min");
    }

    @Test
    @DisplayName("Salida: sin una entrada abierta no se puede registrar")
    void registrarSalida_sinEntrada_lanzaExcepcion() {
        when(repositorioPersona.findByDniAndActivoTrue(DNI)).thenReturn(Optional.of(persona));
        when(repositorioAcceso.findFirstByPersonaIdAndFechaHoraSalidaIsNull(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrarSalida(DNI))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no tiene una entrada abierta");
    }
}
