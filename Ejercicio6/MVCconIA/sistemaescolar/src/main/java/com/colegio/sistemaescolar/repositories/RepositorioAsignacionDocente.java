package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.AsignacionDocente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORIO de {@link AsignacionDocente}. Los {@code @EntityGraph} traen grado y materia en la misma consulta
 * (evita el problema N+1 al listar y al armar los permisos).
 */
@Repository
public interface RepositorioAsignacionDocente extends JpaRepository<AsignacionDocente, Long> {

    @EntityGraph(attributePaths = {"grado", "materia"})
    List<AsignacionDocente> findByDocenteId(long docenteId);

    /** Todas las asignaciones de docentes vigentes (para el listado, en una sola consulta). */
    @EntityGraph(attributePaths = {"docente", "grado", "materia"})
    List<AsignacionDocente> findByDocenteEliminadoFalse();
}
