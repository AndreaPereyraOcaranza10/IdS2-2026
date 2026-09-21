package com.club.fitnessclub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.club.fitnessclub.dtos.ImagenRostroDTO;
import com.club.fitnessclub.dtos.PersonaDTO;
import com.club.fitnessclub.entities.GrupoFamiliar;
import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorPersona;
import com.club.fitnessclub.repositories.RepositorioGrupoFamiliar;
import com.club.fitnessclub.repositories.RepositorioPersona;

/**
 * PRUEBAS UNITARIAS de ServicioPersona (reglas de negocio de personas y foto de rostro).
 *
 * <p>Técnica: los repositorios son dobles de prueba (Mockito), por lo que NO se necesita base de
 * datos ni contexto de Spring: cada test corre en milisegundos y prueba una regla aislada.
 * Patrón AAA: Arrange (preparar), Act (ejecutar), Assert (verificar).
 */
@ExtendWith(MockitoExtension.class)
class ServicioPersonaTest {

    @Mock private RepositorioPersona repositorioPersona;
    @Mock private RepositorioGrupoFamiliar repositorioGrupo;

    private ServicioPersona servicio;
    private GrupoFamiliar grupo;

    @BeforeEach
    void preparar() {
        servicio = new ServicioPersona(repositorioPersona, repositorioGrupo, new MapeadorPersona());
        grupo = DatosDePrueba.grupo(1L, "Familia Pérez");
    }

    private PersonaDTO dto(String dni, Parentesco parentesco) {
        PersonaDTO dto = new PersonaDTO();
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setDni(dni);
        dto.setParentesco(parentesco);
        dto.setGrupoFamiliarId(1L);
        return dto;
    }

    private ImagenRostroDTO imagenJpegValida() {
        return new ImagenRostroDTO("foto.jpg", "image/jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00 });
    }

    @Test
    @DisplayName("Alta: el primer integrante titular se guarda correctamente")
    void save_primerIntegranteTitular_seGuarda() throws Exception {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioPersona.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

        PersonaDTO resultado = servicio.save(dto("30123456", Parentesco.TITULAR));

        assertThat(resultado.getDni()).isEqualTo("30123456");
        assertThat(resultado.getParentesco()).isEqualTo(Parentesco.TITULAR);
        assertThat(resultado.getGrupoFamiliarNombre()).isEqualTo("Familia Pérez");
        assertThat(grupo.getIntegrantes()).hasSize(1); // se sincronizó el lado inverso de la relación
    }

    @Test
    @DisplayName("Alta: rechaza un DNI ya registrado")
    void save_dniDuplicado_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioPersona.existsByDni("30123456")).thenReturn(true);

        assertThatThrownBy(() -> servicio.save(dto("30123456", Parentesco.TITULAR)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("Ya existe una persona");
        verify(repositorioPersona, never()).save(any());
    }

    @Test
    @DisplayName("Alta: rechaza un segundo titular en la misma familia")
    void save_segundoTitular_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));
        when(repositorioPersona.existsByGrupoFamiliarIdAndParentescoAndActivoTrue(1L, Parentesco.TITULAR)).thenReturn(true);

        assertThatThrownBy(() -> servicio.save(dto("30123456", Parentesco.TITULAR)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("ya tiene un titular");
    }

    @Test
    @DisplayName("Alta: el primer integrante de una familia debe ser el titular")
    void save_primerIntegranteNoTitular_lanzaExcepcion() {
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> servicio.save(dto("30123456", Parentesco.HIJO)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("debe ser el titular");
    }

    @Test
    @DisplayName("Alta: rechaza una familia inexistente o dada de baja")
    void save_familiaDeBaja_lanzaExcepcion() {
        grupo.setActivo(false);
        when(repositorioGrupo.findById(1L)).thenReturn(Optional.of(grupo));

        assertThatThrownBy(() -> servicio.save(dto("30123456", Parentesco.TITULAR)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("familia");
    }

    @Test
    @DisplayName("Alta con foto: la foto es obligatoria")
    void saveConImagen_sinImagen_lanzaExcepcion() {
        assertThatThrownBy(() -> servicio.save(dto("30123456", Parentesco.TITULAR), null))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("obligatoria");
        verify(repositorioPersona, never()).save(any());
    }

    @Test
    @DisplayName("Foto: rechaza formatos que no son JPEG/PNG")
    void guardarRostro_formatoNoPermitido_lanzaExcepcion() {
        ImagenRostroDTO gif = new ImagenRostroDTO("a.gif", "image/gif", new byte[] { 1, 2, 3, 4, 5 });

        assertThatThrownBy(() -> servicio.guardarRostro(1L, gif))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("Formato no permitido");
    }

    @Test
    @DisplayName("Foto: rechaza un archivo que declara ser JPEG pero no lo es (firma falsa)")
    void guardarRostro_firmaFalsa_lanzaExcepcion() {
        ImagenRostroDTO falsa = new ImagenRostroDTO("a.jpg", "image/jpeg", new byte[] { 1, 2, 3, 4, 5 });

        assertThatThrownBy(() -> servicio.guardarRostro(1L, falsa))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no es una imagen");
    }

    @Test
    @DisplayName("Foto: rechaza imágenes de más de 3 MB")
    void guardarRostro_demasiadoGrande_lanzaExcepcion() {
        byte[] grande = new byte[3 * 1024 * 1024 + 1];
        grande[0] = (byte) 0xFF; grande[1] = (byte) 0xD8; grande[2] = (byte) 0xFF;

        assertThatThrownBy(() -> servicio.guardarRostro(1L, new ImagenRostroDTO("a.jpg", "image/jpeg", grande)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("3 MB");
    }

    @Test
    @DisplayName("Foto: una imagen JPEG válida se asocia a la persona")
    void guardarRostro_jpegValido_seGuarda() throws Exception {
        Persona persona = DatosDePrueba.persona(7L, "30123456", grupo, Parentesco.TITULAR);
        when(repositorioPersona.findById(7L)).thenReturn(Optional.of(persona));

        servicio.guardarRostro(7L, imagenJpegValida());

        assertThat(persona.getImagenRostro()).isNotNull();
        assertThat(persona.getImagenRostro().getContentType()).isEqualTo("image/jpeg");
        verify(repositorioPersona).save(persona);
    }

    @Test
    @DisplayName("Baja: no se puede dar de baja al titular si quedan otros integrantes")
    void delete_titularConOtrosIntegrantes_lanzaExcepcion() {
        Persona titular = DatosDePrueba.persona(1L, "30000001", grupo, Parentesco.TITULAR);
        when(repositorioPersona.findById(1L)).thenReturn(Optional.of(titular));
        when(repositorioPersona.countByGrupoFamiliarIdAndActivoTrue(1L)).thenReturn(3L);

        assertThatThrownBy(() -> servicio.delete(1L))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("titular");
        assertThat(titular.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Baja: un integrante común se da de baja (baja lógica)")
    void delete_integranteComun_seMarcaInactivo() throws Exception {
        Persona hijo = DatosDePrueba.persona(2L, "30000002", grupo, Parentesco.HIJO);
        when(repositorioPersona.findById(2L)).thenReturn(Optional.of(hijo));

        servicio.delete(2L);

        assertThat(hijo.isActivo()).isFalse();
        verify(repositorioPersona).save(hijo);
    }

    @Test
    @DisplayName("Alta: si no se elige familia existente, se crea una nueva con el titular")
    void save_conFamiliaNueva_creaFamiliaYTitular() throws Exception {
        when(repositorioGrupo.save(any(GrupoFamiliar.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repositorioPersona.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));
        PersonaDTO dto = dto("30123456", Parentesco.TITULAR);
        dto.setGrupoFamiliarId(null);
        dto.setNuevaFamiliaNombre("Familia Nueva");

        PersonaDTO resultado = servicio.save(dto);

        assertThat(resultado.getGrupoFamiliarNombre()).isEqualTo("Familia Nueva");
        verify(repositorioGrupo).save(any(GrupoFamiliar.class));
    }

    @Test
    @DisplayName("Alta: sin familia existente ni nombre de familia nueva se rechaza")
    void save_sinFamiliaNiNombre_lanzaExcepcion() {
        PersonaDTO dto = dto("30123456", Parentesco.TITULAR);
        dto.setGrupoFamiliarId(null);

        assertThatThrownBy(() -> servicio.save(dto))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("familia nueva");
        verify(repositorioGrupo, never()).save(any());
    }
}
