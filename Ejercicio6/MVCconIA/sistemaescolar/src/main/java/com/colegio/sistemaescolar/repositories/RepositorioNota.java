package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Nota;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO de {@link Nota}.
 *
 * <p>{@code buscarVisibles} implementa la regla "un docente solo ve las notas de los alumnos de un grado
 * en las materias que dicta EN ESE grado (existe una AsignacionDocente que coincide)" directamente en la consulta (join fetch trae alumno y materia sin N+1).</p>
 */
@Repository
public interface RepositorioNota extends JpaRepository<Nota, Long> {

    @EntityGraph(attributePaths = {"alumno", "materia"})
    List<Nota> findByEliminadoFalseOrderByFechaDescIdDesc();

    @Query("select n from Nota n join fetch n.alumno a join fetch n.materia m "
            + "where n.eliminado = false and exists ("
            + "select 1 from AsignacionDocente ad where ad.docente.id = :docenteId "
            + "and ad.grado.id = a.grado.id and ad.materia.id = m.id) "
            + "order by n.fecha desc, n.id desc")
    List<Nota> buscarVisibles(@Param("docenteId") long docenteId);

    @EntityGraph(attributePaths = {"alumno", "materia"})
    Optional<Nota> findByIdAndEliminadoFalse(long id);

    /** Notas de un alumno (para armar el boletín y para darlas de baja junto con el alumno). */
    @EntityGraph(attributePaths = {"materia"})
    List<Nota> findByAlumnoIdAndEliminadoFalseOrderByFechaAscIdAsc(long alumnoId);

    boolean existsByMateriaIdAndEliminadoFalse(long materiaId);

    long countByEliminadoFalse();

    /** Promedio de todas las notas vigentes (null si no hay notas). */
    @Query("select avg(n.valor) from Nota n where n.eliminado = false")
    Double promedioGeneral();
}
