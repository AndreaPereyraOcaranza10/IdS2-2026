package com.isw2.accesousuarios.repositories;

import com.isw2.accesousuarios.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CAPA: REPOSITORIO (acceso a datos)
 * ============================================================================
 * Acceso a la tabla "personas". Al extender JpaRepository<Persona, Long>, Spring
 * Data JPA genera EN TIEMPO DE EJECUCIÓN la implementación con las operaciones
 * CRUD (save, findById, findAll, delete, ...). No hay que escribir SQL ni la clase.
 *
 * ANOTACIÓN
 *   @Repository : registra la interfaz como bean de la capa de persistencia y
 *                 traduce las excepciones de la base a excepciones de Spring
 *                 (DataAccessException).
 *
 * MÉTODOS DERIVADOS (query methods): Spring Data interpreta el NOMBRE del método
 * y arma la consulta. existsByCorreo(x) => SELECT ... WHERE correo = x LIMIT 1.
 */
@Repository
public interface RepositorioPersona extends JpaRepository<Persona, Long> {

    /** Indica si ya hay una persona registrada con ese correo (regla de unicidad). */
    boolean existsByCorreo(String correo);

    /** Indica si ya hay una persona registrada con ese documento (regla de unicidad). */
    boolean existsByDocumento(String documento);
}
