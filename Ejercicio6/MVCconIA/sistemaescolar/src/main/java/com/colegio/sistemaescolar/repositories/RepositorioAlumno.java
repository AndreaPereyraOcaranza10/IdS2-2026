package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Alumno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO de {@link Alumno}.
 *
 * <p>{@code @EntityGraph(attributePaths = {"grado", "aula"})} le indica a JPA que traiga el grado y el
 * aula en la MISMA consulta (JOIN) y así evita el problema "N+1" (una consulta extra por cada alumno
 * al mostrar la lista).</p>
 */
@Repository
public interface RepositorioAlumno extends JpaRepository<Alumno, Long> {

    @EntityGraph(attributePaths = {"grado", "aula"})
    List<Alumno> findByEliminadoFalseOrderByApellidoAscNombreAsc();

    /** Alumnos de ciertos grados (los que tiene asignados un docente). */
    @EntityGraph(attributePaths = {"grado", "aula"})
    List<Alumno> findByGradoIdInAndEliminadoFalseOrderByApellidoAscNombreAsc(Collection<Long> gradoIds);

    @EntityGraph(attributePaths = {"grado", "aula"})
    Optional<Alumno> findByIdAndEliminadoFalse(long id);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, long id);

    boolean existsByGradoIdAndEliminadoFalse(long gradoId);

    boolean existsByAulaIdAndEliminadoFalse(long aulaId);

    long countByGradoIdAndEliminadoFalse(long gradoId);

    long countByAulaIdAndEliminadoFalse(long aulaId);

    long countByEliminadoFalse();

    long countByGradoIdInAndEliminadoFalse(Collection<Long> gradoIds);
}
