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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.club.fitnessclub.dtos.UsuarioDTO;
import com.club.fitnessclub.entities.Usuario;
import com.club.fitnessclub.enums.Rol;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.mappers.MapeadorUsuario;
import com.club.fitnessclub.repositories.RepositorioUsuario;

/** PRUEBAS UNITARIAS de ServicioUsuario: seguridad de contraseñas y protección del último ADMIN. */
@ExtendWith(MockitoExtension.class)
class ServicioUsuarioTest {

    @Mock private RepositorioUsuario repositorioUsuario;
    @Mock private PasswordEncoder passwordEncoder;

    private ServicioUsuario servicio;

    @BeforeEach
    void preparar() {
        servicio = new ServicioUsuario(repositorioUsuario, new MapeadorUsuario(), passwordEncoder);
    }

    private UsuarioDTO dto(String username, String password, String confirmar, Rol rol) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setConfirmarPassword(confirmar);
        dto.setRol(rol);
        return dto;
    }

    private Usuario usuario(long id, String username, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("HASH-VIEJO");
        u.setRol(rol);
        return u;
    }

    @Test
    @DisplayName("Alta: la contraseña se guarda hasheada, nunca en texto plano")
    void save_hasheaLaPassword() throws Exception {
        when(passwordEncoder.encode("clave1234")).thenReturn("HASH-BCRYPT");
        when(repositorioUsuario.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = servicio.save(dto("maria", "clave1234", "clave1234", Rol.RECEPCION));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorioUsuario).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("HASH-BCRYPT");
        assertThat(resultado.getPassword()).isNull(); // el DTO devuelto nunca expone la contraseña
    }

    @Test
    @DisplayName("Alta: la contraseña es obligatoria")
    void save_sinPassword_lanzaExcepcion() {
        assertThatThrownBy(() -> servicio.save(dto("maria", "", "", Rol.RECEPCION)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("obligatoria");
        verify(repositorioUsuario, never()).save(any());
    }

    @Test
    @DisplayName("Alta: rechaza contraseñas de menos de 8 caracteres")
    void save_passwordCorta_lanzaExcepcion() {
        assertThatThrownBy(() -> servicio.save(dto("maria", "corta", "corta", Rol.RECEPCION)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("al menos 8");
    }

    @Test
    @DisplayName("Alta: la confirmación debe coincidir")
    void save_confirmacionDistinta_lanzaExcepcion() {
        assertThatThrownBy(() -> servicio.save(dto("maria", "clave1234", "otraclave", Rol.RECEPCION)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no coinciden");
    }

    @Test
    @DisplayName("Alta: rechaza un username repetido")
    void save_usernameDuplicado_lanzaExcepcion() {
        when(repositorioUsuario.existsByUsername("maria")).thenReturn(true);

        assertThatThrownBy(() -> servicio.save(dto("maria", "clave1234", "clave1234", Rol.RECEPCION)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    @DisplayName("Modificación: con contraseña vacía se conserva la actual (validación condicional)")
    void update_sinPassword_conservaHashActual() throws Exception {
        Usuario existente = usuario(3L, "maria", Rol.RECEPCION);
        when(repositorioUsuario.findById(3L)).thenReturn(Optional.of(existente));
        when(repositorioUsuario.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.update(3L, dto("maria", null, null, Rol.RECEPCION));

        assertThat(existente.getPassword()).isEqualTo("HASH-VIEJO");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Baja: no se puede dar de baja al último administrador")
    void delete_ultimoAdmin_lanzaExcepcion() {
        Usuario admin = usuario(1L, "admin", Rol.ADMIN);
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(admin));
        when(repositorioUsuario.countByRolAndActivoTrue(Rol.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> servicio.delete(1L))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("último ADMIN");
        assertThat(admin.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Modificación: no se puede degradar al último administrador")
    void update_degradarUltimoAdmin_lanzaExcepcion() {
        Usuario admin = usuario(1L, "admin", Rol.ADMIN);
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(admin));
        when(repositorioUsuario.countByRolAndActivoTrue(Rol.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> servicio.update(1L, dto("admin", null, null, Rol.RECEPCION)))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("último ADMIN");
    }

    @Test
    @DisplayName("Baja: un administrador se puede dar de baja si queda otro")
    void delete_adminConOtroAdmin_seDaDeBaja() throws Exception {
        Usuario admin = usuario(1L, "admin", Rol.ADMIN);
        when(repositorioUsuario.findById(1L)).thenReturn(Optional.of(admin));
        when(repositorioUsuario.countByRolAndActivoTrue(Rol.ADMIN)).thenReturn(2L);

        servicio.delete(1L);

        assertThat(admin.isActivo()).isFalse();
    }
}
