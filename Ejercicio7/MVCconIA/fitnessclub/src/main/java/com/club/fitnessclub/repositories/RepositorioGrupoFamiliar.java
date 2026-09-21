package com.club.fitnessclub.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.GrupoFamiliar;

/**
 * Repositorio de GrupoFamiliar. Spring Data genera la implementación en tiempo de ejecución;
 * JpaRepository ya aporta save, findById, findAll, count, etc.
 */
@Repository
public interface RepositorioGrupoFamiliar extends JpaRepository<GrupoFamiliar, Long> {

    /**
     * Familias vigentes CON sus integrantes en UNA sola consulta (LEFT JOIN FETCH).
     * Sin el fetch, mapear cada familia dispararía una consulta adicional por familia para leer
     * sus integrantes (problema N+1). LEFT JOIN: las familias sin integrantes también aparecen.
     * DISTINCT evita duplicar cada familia por integrante.
     */
    @Query("select distinct g from GrupoFamiliar g left join fetch g.integrantes "
            + "where g.activo = true order by g.nombre")
    List<GrupoFamiliar> findAllActivosConIntegrantes();

    /** Cantidad de familias vigentes (tablero del panel). */
    long countByActivoTrue();

    /** Familias vigentes sin cargar integrantes (emisión masiva de cuotas). */
    List<GrupoFamiliar> findByActivoTrueOrderByNombreAsc();
}
