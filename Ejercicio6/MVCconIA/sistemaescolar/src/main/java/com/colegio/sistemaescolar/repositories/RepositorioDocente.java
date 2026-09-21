package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Docente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO de {@link Docente}.
 *
 * <p>Las consultas {@code @Query} usan JPQL (lenguaje de consultas orientado a entidades, no a tablas).
 * Sirven para preguntar si un grado o una materia están asignados a algún docente activo antes de
 * permitir su baja.</p>
 */
@Repository
public interface RepositorioDocente extends JpaRepository<Docente, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    List<Docente> findByEliminadoFalseOrderByApellidoAscNombreAsc();

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Docente> findByIdAndEliminadoFalse(long id);

    /** Docente activo asociado a un correo (el correo es el nombre de usuario). */
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Docente> findByUsuarioEmailIgnoreCaseAndEliminadoFalse(String email);

    long countByEliminadoFalse();

    @Query("select count(a) > 0 from AsignacionDocente a where a.grado.id = :gradoId and a.docente.eliminado = false")
    boolean existeDocenteConGrado(@Param("gradoId") long gradoId);

    @Query("select count(a) > 0 from AsignacionDocente a where a.materia.id = :materiaId and a.docente.eliminado = false")
    boolean existeDocenteConMateria(@Param("materiaId") long materiaId);
}
