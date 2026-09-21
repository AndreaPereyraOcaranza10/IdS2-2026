package com.club.fitnessclub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.club.fitnessclub.DatosDePrueba;
import com.club.fitnessclub.dtos.GrupoFamiliarDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.enums.EstadoCuota;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioCuota;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;

/** PRUEBAS UNITARIAS de ServicioGrupoFamiliar (ABM de familias y reglas de baja). */
@ExtendWith(MockitoExtension.class)
class ServicioGrupoFamiliarTest {

    @Mock private RepositorioGrupoFamiliar repositorioGrupo;
    @Mock private RepositorioCuota repositorioCuota;

    private ServicioGrupoFamiliar servicio;
    private GrupoFamiliar grupo;

    @BeforeEach
    void preparar() {
        servicio = new ServicioGrupoFamiliar(repositorioGrupo, repositorioCuota, new MapeadorGrupoFamiliar());
        grupo = DatosDePrueba.grupo(1L, "Familia Gómez");
    }

    @Test
    @DisplayName("findById: informa el titular y la cantidad de integrantes vigentes")
    void findById_calculaTitularYCantidad() throws Exception {
        DatosDePrueba.persona(1L, "30000001", grupo, Parentesco.TITULAR);
        DatosDePrueba.persona(2L, "30000002", grupo, Parentesco.HIJO);
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        GrupoFamiliarDTO dto = servicio.findById(1L);

        assertThat(dto.getCantidadIntegrantes()).isEqualTo(2);
        assertThat(dto.getTitularNombre()).isEqualTo("Apellido1, Nombre1");
    }

    @Test
    @DisplayName("findById: una familia dada de baja se trata como inexistente")
    void findById_familiaDeBaja_lanzaExcepcion() {
        grupo.setActivo(false);
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> servicio.findById(1L)).isInstanceOf(ExcepcionNegocio.class);
    }

    @Test
    @DisplayName("Baja: no se permite si la familia tiene cuotas pendientes")
    void delete_conCuotasPendientes_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioCuota.existsByGrupoFamiliarIdAndEstadoAndActivoTrue(1L, EstadoCuota.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> servicio.delete(1L))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("cuotas pendientes");
        assertThat(grupo.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Baja: al dar de baja la familia también se dan de baja sus integrantes")
    void delete_sinDeuda_dabajaFamiliaEIntegrantes() throws Exception {
        var titular = DatosDePrueba.persona(1L, "30000001", grupo, Parentesco.TITULAR);
        var hijo = DatosDePrueba.persona(2L, "30000002", grupo, Parentesco.HIJO);
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        servicio.delete(1L);

        assertThat(grupo.isActivo()).isFalse();
        assertThat(titular.isActivo()).isFalse();
        assertThat(hijo.isActivo()).isFalse();
        verify(repositorioGrupo).save(grupo);
    }
}
