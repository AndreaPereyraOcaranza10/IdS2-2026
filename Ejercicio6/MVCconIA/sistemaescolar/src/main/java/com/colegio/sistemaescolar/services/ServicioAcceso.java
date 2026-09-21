package com.colegio.sistemaescolar.services;

import com.colegio.sistemaescolar.entities.AsignacionDocente;
import com.colegio.sistemaescolar.entities.Docente;
import com.colegio.sistemaescolar.repositories.RepositorioAsignacionDocente;
import com.colegio.sistemaescolar.repositories.RepositorioDocente;
import com.colegio.sistemaescolar.security.ContextoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ALCANCE de datos del usuario actual: responde "¿qué alumnos puede ver y en qué materias puede calificar?".
 *
 * <ul>
 *   <li>ADMIN: ve todo (los servicios consultan {@link #esAdmin()} y no filtran).</li>
 *   <li>DOCENTE: solo lo que surge de sus {@link AsignacionDocente} (terna docente + grado + materia).</li>
 * </ul>
 *
 * <h2>Regla clave</h2>
 * Un docente califica a un alumno en una materia solo si tiene una asignación cuyo grado es el del alumno Y cuya
 * materia es la pedida ({@link #puedeCalificar}). Tener "Matemática" en 1.º Grado y "Lengua" en 2.º Grado NO lo habilita
 * a calificar Lengua en 1.º.
 *
 * <p>Centralizar esta lógica evita repetirla en cada servicio.</p>
 */
@Component
@RequiredArgsConstructor
public class ServicioAcceso {

    private final ContextoUsuario contexto;
    private final RepositorioDocente repositorioDocente;
    private final RepositorioAsignacionDocente repositorioAsignacion;

    public boolean esAdmin() {
        return contexto.esAdmin();
    }

    /** Ficha del docente autenticado (vacío si es ADMIN o no hay sesión). */
    @Transactional(readOnly = true)
    public Optional<Docente> docenteActual() {
        return contexto.email()
                .flatMap(repositorioDocente::findByUsuarioEmailIgnoreCaseAndEliminadoFalse);
    }

    /** Asignaciones (con grado y materia ya cargados) del docente autenticado; vacía si no es docente. */
    @Transactional(readOnly = true)
    public List<AsignacionDocente> asignacionesActuales() {
        return docenteActual()
                .map(d -> repositorioAsignacion.findByDocenteId(d.getId()))
                .orElseGet(ArrayList::new);
    }

    /** Grados en los que el docente dicta al menos una materia (define qué alumnos puede ver). */
    @Transactional(readOnly = true)
    public Set<Long> idsGradosPermitidos() {
        return asignacionesActuales().stream()
                .map(a -> a.getGrado().getId())
                .collect(Collectors.toSet());
    }

    /** Materias que el docente dicta en algún grado (para poblar el desplegable; la validación exacta es por par). */
    @Transactional(readOnly = true)
    public Set<Long> idsMateriasPermitidas() {
        return asignacionesActuales().stream()
                .map(a -> a.getMateria().getId())
                .collect(Collectors.toSet());
    }

    /** ¿Existe una asignación exacta grado + materia para el docente autenticado? El ADMIN siempre puede. */
    @Transactional(readOnly = true)
    public boolean puedeCalificar(long gradoId, long materiaId) {
        if (esAdmin()) {
            return true;
        }
        return asignacionesActuales().stream()
                .anyMatch(a -> a.getGrado().getId() == gradoId && a.getMateria().getId() == materiaId);
    }
}
