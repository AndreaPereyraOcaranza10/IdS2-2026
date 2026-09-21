package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.config.PropiedadesColegio;
import com.colegio.sistemaescolar.dtos.DocenteDTO;
import com.colegio.sistemaescolar.dtos.RegistroDocenteDTO;
import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.enums.Rol;
import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.events.DocenteRegistradoEvento;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.mappers.MapeadorDocente;
import com.colegio.sistemaescolar.repositories.RepositorioAsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.repositories.RepositorioGrado;
import com.colegio.sistemaescolar.repositories.RepositorioMateria;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitarios (sin Spring ni base de datos) de las reglas de registro de docentes. Los repositorios se simulan con Mockito. */
class ServicioDocenteTest {

    private RepositorioDocente repositorio;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioAsignacionDocente repositorioAsignacion;
    private RepositorioGrado repositorioGrado;
    private RepositorioMateria repositorioMateria;
    private ApplicationEventPublisher publicador;
    private PropiedadesColegio propiedades;
    private PasswordEncoder codificador;
    private ServicioDocente servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioDocente.class);
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioAsignacion = mock(RepositorioAsignacionDocente.class);
        repositorioGrado = mock(RepositorioGrado.class);
        repositorioMateria = mock(RepositorioMateria.class);
        publicador = mock(ApplicationEventPublisher.class);
        propiedades = new PropiedadesColegio();
        codificador = new BCryptPasswordEncoder();
        // save() devuelve el mismo objeto que recibe
        when(repositorioUsuario.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(repositorio.save(any(Docente.class))).thenAnswer(i -> i.getArgument(0));
        servicio = new ServicioDocente(repositorio, repositorioUsuario, repositorioGrado,
                repositorioMateria, repositorioAsignacion, new MapeadorDocente(), codificador, publicador, propiedades,
                Clock.systemDefaultZone());
    }

    private RegistroDocenteDTO dtoValido() {
        RegistroDocenteDTO dto = new RegistroDocenteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Ramírez");
        dto.setSexo(Sexo.FEMENINO);
        dto.setFechaNacimiento(LocalDate.of(1985, 4, 12));
        dto.setEmail("  Ana.Ramirez@Correo.com ");
        dto.setPassword("Clave1234");
        dto.setConfirmarPassword("Clave1234");
        return dto;
    }

    @Test
    void registrarGuardaUsuarioConPasswordCifradaYPublicaEvento() throws Exception {
        servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorioUsuario).save(captor.capture());
        Usuario usuario = captor.getValue();
        assertThat(usuario.getEmail()).isEqualTo("ana.ramirez@correo.com");   // normalizado
        assertThat(usuario.getRol()).isEqualTo(Rol.DOCENTE);
        assertThat(usuario.isHabilitado()).isTrue();
        assertThat(usuario.getPasswordHash()).isNotEqualTo("Clave1234");
        assertThat(codificador.matches("Clave1234", usuario.getPasswordHash())).isTrue();

        ArgumentCaptor<DocenteRegistradoEvento> evento = ArgumentCaptor.forClass(DocenteRegistradoEvento.class);
        verify(publicador).publishEvent(evento.capture());
        assertThat(evento.getValue().email()).isEqualTo("ana.ramirez@correo.com");
        assertThat(evento.getValue().pendienteAprobacion()).isFalse();
    }

    @Test
    void conAprobacionRequeridaLaCuentaNaceDeshabilitada() throws Exception {
        propiedades.getRegistro().setRequiereAprobacion(true);
        servicio.registrar(dtoValido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorioUsuario).save(captor.capture());
        assertThat(captor.getValue().isHabilitado()).isFalse();
    }

    @Test
    void rechazaCorreoRepetido() {
        when(repositorioUsuario.existsByEmailIgnoreCase("ana.ramirez@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(dtoValido()))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("Ya existe una cuenta");
        verify(repositorioUsuario, never()).save(any());
        verify(publicador, never()).publishEvent(any(Object.class));
    }

    @Test
    void rechazaContraseniasDistintas() {
        RegistroDocenteDTO dto = dtoValido();
        dto.setConfirmarPassword("Otra1234");

        assertThatThrownBy(() -> servicio.registrar(dto))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no coinciden");
        verify(repositorio, never()).save(any());
    }

    @Test
    void registrarDevuelveDtoConDatosPersonales() throws Exception {
        DocenteDTO resultado = servicio.registrar(dtoValido());

        assertThat(resultado.getNombre()).isEqualTo("Ana");
        assertThat(resultado.getApellido()).isEqualTo("Ramírez");
        assertThat(resultado.getEmail()).isEqualTo("ana.ramirez@correo.com");
    }

    // ---------------------------------------------------------------- asignaciones (docente + grado + materia)

    private Docente docenteExistente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@correo.com");
        usuario.setRol(Rol.DOCENTE);
        Docente docente = new Docente();
        docente.setNombre("Ana");
        docente.setApellido("Ramírez");
        docente.setSexo(Sexo.FEMENINO);
        docente.setFechaNacimiento(LocalDate.of(1985, 4, 12));
        docente.setUsuario(usuario);
        when(repositorio.findByIdAndEliminadoFalse(1L)).thenReturn(Optional.of(docente));
        return docente;
    }

    private Grado grado(long id, String nombre) {
        Grado g = new Grado();
        g.setId(id);
        g.setNombre(nombre);
        g.setNivel((int) id);
        when(repositorioGrado.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(g));
        return g;
    }

    private Materia materia(long id, String nombre) {
        Materia m = new Materia();
        m.setId(id);
        m.setNombre(nombre);
        when(repositorioMateria.findByIdAndEliminadoFalse(id)).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void actualizarCreaLasAsignacionesMarcadas() throws Exception {
        docenteExistente();
        grado(1, "1.º Grado");
        materia(10, "Matemática");
        when(repositorioAsignacion.findByDocenteId(0L)).thenReturn(List.of());
        when(repositorioAsignacion.save(any(AsignacionDocente.class))).thenAnswer(i -> i.getArgument(0));

        DocenteDTO dto = new DocenteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Ramírez");
        dto.setSexo(Sexo.FEMENINO);
        dto.setFechaNacimiento(LocalDate.of(1985, 4, 12));
        dto.setHabilitado(true);
        dto.setAsignaciones(Set.of("1-10"));

        DocenteDTO resultado = servicio.actualizar(1L, dto);

        ArgumentCaptor<AsignacionDocente> captor = ArgumentCaptor.forClass(AsignacionDocente.class);
        verify(repositorioAsignacion).save(captor.capture());
        assertThat(captor.getValue().getGrado().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getMateria().getId()).isEqualTo(10L);
        assertThat(resultado.getAsignacionesTexto()).containsExactly("1.º Grado - Matemática");
    }

    @Test
    void actualizarBorraLasAsignacionesDesmarcadas() throws Exception {
        Docente docente = docenteExistente();
        AsignacionDocente vieja = new AsignacionDocente();
        vieja.setDocente(docente);
        vieja.setGrado(grado(1, "1.º Grado"));
        vieja.setMateria(materia(10, "Matemática"));
        when(repositorioAsignacion.findByDocenteId(0L)).thenReturn(List.of(vieja));

        DocenteDTO dto = new DocenteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Ramírez");
        dto.setSexo(Sexo.FEMENINO);
        dto.setFechaNacimiento(LocalDate.of(1985, 4, 12));
        dto.setAsignaciones(Set.of());   // se desmarcó todo

        servicio.actualizar(1L, dto);

        verify(repositorioAsignacion).delete(vieja);
        verify(repositorioAsignacion, never()).save(any());
    }

    @Test
    void rechazaAsignacionesConFormatoInvalido() {
        docenteExistente();
        DocenteDTO dto = new DocenteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Ramírez");
        dto.setSexo(Sexo.FEMENINO);
        dto.setFechaNacimiento(LocalDate.of(1985, 4, 12));
        dto.setAsignaciones(Set.of("abc"));

        assertThatThrownBy(() -> servicio.actualizar(1L, dto))
                .isInstanceOf(ExcepcionNegocio.class)
                .hasMessageContaining("no son válidas");
    }
}
