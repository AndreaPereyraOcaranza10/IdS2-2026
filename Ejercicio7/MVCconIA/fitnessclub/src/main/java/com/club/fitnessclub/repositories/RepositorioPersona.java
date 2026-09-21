package com.club.fitnessclub.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.Persona;
import com.club.fitnessclub.enums.Parentesco;

/**
 * Repositorio de Persona. Las consultas son "derivadas": Spring las genera a partir del NOMBRE
 * del método (findBy..., existsBy..., countBy...).
 *
 * <p>@EntityGraph(attributePaths = "grupoFamiliar"): carga la familia con un JOIN en la misma
 * consulta, porque el mapeador necesita su nombre y la relación es LAZY.
 */
@Repository
public interface RepositorioPersona extends JpaRepository<Persona, Long> {

    /** Personas vigentes ordenadas alfabéticamente (listado general). */
    @EntityGraph(attributePaths = "grupoFamiliar")
    List<Persona> findByActivoTrueOrderByApellidoAscNombreAsc();

    /** Integrantes vigentes de una familia. "GrupoFamiliarId" navega la propiedad grupoFamiliar.id. */
    @EntityGraph(attributePaths = "grupoFamiliar")
    List<Persona> findByGrupoFamiliarIdAndActivoTrueOrderByApellidoAscNombreAsc(long grupoFamiliarId);

    /** Búsqueda por DNI en recepción (solo personas vigentes). */
    @EntityGraph(attributePaths = "grupoFamiliar")
    Optional<Persona> findByDniAndActivoTrue(String dni);

    /**
     * ¿Existe el DNI? Se consulta SIN filtrar por activo porque la restricción única de la BD
     * (uk_persona_dni) incluye a las personas dadas de baja.
     */
    boolean existsByDni(String dni);

    /** Igual que existsByDni pero excluyendo a la propia persona (para modificaciones). */
    boolean existsByDniAndIdNot(String dni, long id);

    /** ¿La familia ya tiene un integrante vigente con ese parentesco? (regla del titular). */
    boolean existsByGrupoFamiliarIdAndParentescoAndActivoTrue(long grupoFamiliarId, Parentesco parentesco);

    /** Cantidad de integrantes vigentes de una familia. */
    long countByGrupoFamiliarIdAndActivoTrue(long grupoFamiliarId);

    /** Cantidad de personas vigentes (tablero del panel). */
    long countByActivoTrue();
}
