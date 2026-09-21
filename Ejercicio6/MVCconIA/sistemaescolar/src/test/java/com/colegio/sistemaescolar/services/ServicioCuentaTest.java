package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.dtos.CambioPasswordDTO;
import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.enums.Rol;
import com.colegio.sistemaescolar.events.PasswordCambiadaEvento;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import com.colegio.sistemaescolar.security.ContextoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitarios del cambio de contraseña del usuario autenticado. */
class ServicioCuentaTest {

    private RepositorioUsuario repositorio;
    private ApplicationEventPublisher publicador;
    private PasswordEncoder codificador;
    private ServicioCuenta servicio;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioUsuario.class);
        publicador = mock(ApplicationEventPublisher.class);
        codificador = new BCryptPasswordEncoder();
        ContextoUsuario contexto = mock(ContextoUsuario.class);
        ServicioAcceso acceso = mock(ServicioAcceso.class);

        usuario = new Usuario();
        usuario.setEmail("ana@correo.com");
        usuario.setRol(Rol.DOCENTE);
        usuario.setPasswordHash(codificador.encode("Actual1234"));

        when(contexto.email()).thenReturn(Optional.of("ana@correo.com"));
        when(repositorio.findByEmailIgnoreCase("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(acceso.docenteActual()).thenReturn(Optional.empty());
        servicio = new ServicioCuenta(repositorio, acceso, contexto, codificador, publicador, Clock.systemDefaultZone());
    }

    private CambioPasswordDTO dto(String actual, String nueva, String confirmar) {
        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual(actual);
        dto.setPasswordNueva(nueva);
        dto.setConfirmarPassword(confirmar);
        return dto;
    }

    @Test
    void cambiaLaPasswordYPublicaEvento() throws Exception {
        servicio.cambiarPassword(dto("Actual1234", "Nueva5678", "Nueva5678"));

        assertThat(codificador.matches("Nueva5678", usuario.getPasswordHash())).isTrue();
        assertThat(usuario.getUltimoCambioPassword()).isNotNull();
        verify(repositorio).save(usuario);
        ArgumentCaptor<PasswordCambiadaEvento> captor = ArgumentCaptor.forClass(PasswordCambiadaEvento.class);
        verify(publicador).publishEvent(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("ana@correo.com");
    }

    @Test
    void rechazaPasswordActualIncorrecta() {
        assertThatThrownBy(() -> servicio.cambiarPassword(dto("Mala0000", "Nueva5678", "Nueva5678")))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("actual es incorrecta");
        verify(repositorio, never()).save(any());
    }

    @Test
    void rechazaConfirmacionDistinta() {
        assertThatThrownBy(() -> servicio.cambiarPassword(dto("Actual1234", "Nueva5678", "Distinta1")))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    void rechazaNuevaIgualALaActual() {
        assertThatThrownBy(() -> servicio.cambiarPassword(dto("Actual1234", "Actual1234", "Actual1234")))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("distinta");
    }
}
