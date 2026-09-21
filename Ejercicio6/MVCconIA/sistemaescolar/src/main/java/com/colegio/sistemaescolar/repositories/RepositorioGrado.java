package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORIO de {@link Grado}: acceso a datos con Spring Data JPA.
 *
 * <p>No se escribe la implementación: Spring Data genera las consultas a partir del NOMBRE del método
 * ("derived queries"). Ejemplo: {@code findByEliminadoFalseOrderByNivelAsc} =
 * SELECT ... WHERE eliminado = false ORDER BY nivel ASC. {@code JpaRepository<Grado, Long>} aporta
 * además save, findById, count, etc.</p>
 *
 * <p>Las comprobaciones de unicidad ({@code existsBy...}) NO filtran por eliminado a propósito:
 * la restricción UNIQUE de la base también incluye a los registros dados de baja.</p>
 */
@Repository
public interface RepositorioGrado extends JpaRepository<Grado, Long> {

    List<Grado> findByEliminadoFalseOrderByNivelAscNombreAsc();

    Optional<Grado> findByIdAndEliminadoFalse(long id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, long id);

    long countByEliminadoFalse();
}
