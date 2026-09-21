package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** REPOSITORIO de {@link Aula} (consultas derivadas del nombre del método). */
@Repository
public interface RepositorioAula extends JpaRepository<Aula, Long> {

    List<Aula> findByEliminadoFalseOrderByCodigoAsc();

    Optional<Aula> findByIdAndEliminadoFalse(long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, long id);

    long countByEliminadoFalse();
}
