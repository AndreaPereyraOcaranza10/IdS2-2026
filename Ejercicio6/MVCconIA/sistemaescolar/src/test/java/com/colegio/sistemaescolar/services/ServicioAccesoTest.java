package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.entities.Grado;
import com.colegio.sistemaescolar.entities.Materia;
import com.colegio.sistemaescolar.repositories.RepositorioAsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.security.ContextoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de la autorización por asignación exacta (docente + grado + materia):
 * tener la materia en un grado NO habilita a calificarla en otro.
 */
class ServicioAccesoTest {

    private ContextoUsuario contexto;
    private ServicioAcceso acceso;

    @BeforeEach
    void preparar() {
        contexto = mock(ContextoUsuario.class);
        RepositorioDocente repositorioDocente = mock(RepositorioDocente.class);
        RepositorioAsignacionDocente repositorioAsignacion = mock(RepositorioAsignacionDocente.class);

        Docente docente = new Docente();
        docente.setId(7L);
        when(contexto.email()).thenReturn(Optional.of("ana@correo.com"));
        when(repositorioDocente.findByUsuarioEmailIgnoreCaseAndEliminadoFalse("ana@correo.com"))
                .thenReturn(Optional.of(docente));

        // Ana: Matemática en 1.º y Lengua en 2.º
        when(repositorioAsignacion.findByDocenteId(7L)).thenReturn(List.of(
                asignacion(docente, 1, 100), asignacion(docente, 2, 200)));
        acceso = new ServicioAcceso(contexto, repositorioDocente, repositorioAsignacion);
    }

    private AsignacionDocente asignacion(Docente docente, long gradoId, long materiaId) {
        Grado g = new Grado();
        g.setId(gradoId);
        Materia m = new Materia();
        m.setId(materiaId);
        AsignacionDocente a = new AsignacionDocente();
        a.setDocente(docente);
        a.setGrado(g);
        a.setMateria(m);
        return a;
    }

    @Test
    void puedeCalificarSoloLaCombinacionExacta() {
        assertThat(acceso.puedeCalificar(1, 100)).isTrue();
        assertThat(acceso.puedeCalificar(2, 200)).isTrue();
        assertThat(acceso.puedeCalificar(1, 200)).isFalse();   // Lengua no la dicta en 1.º
        assertThat(acceso.puedeCalificar(2, 100)).isFalse();   // Matemática no la dicta en 2.º
        assertThat(acceso.puedeCalificar(3, 100)).isFalse();
    }

    @Test
    void gradosYMateriasPermitidosSonLaUnionDeSusAsignaciones() {
        assertThat(acceso.idsGradosPermitidos()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(acceso.idsMateriasPermitidas()).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    void elAdminSiempreQuedaHabilitado() {
        when(contexto.esAdmin()).thenReturn(true);
        assertThat(acceso.puedeCalificar(99, 99)).isTrue();
    }
}
