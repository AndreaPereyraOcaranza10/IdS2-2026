package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** REPOSITORIO de {@link Materia}. */
@Repository
public interface RepositorioMateria extends JpaRepository<Materia, Long> {

    List<Materia> findByEliminadoFalseOrderByNombreAsc();

    /** Materias de una lista de ids (para las que dicta un docente). */
    List<Materia> findByIdInAndEliminadoFalseOrderByNombreAsc(Collection<Long> ids);

    Optional<Materia> findByIdAndEliminadoFalse(long id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, long id);

    long countByEliminadoFalse();
}
