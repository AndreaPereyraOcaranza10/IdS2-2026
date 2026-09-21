package com.club.fitnessclub.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.RegistroAcceso;

/**
 * Repositorio de RegistroAcceso (entradas y salidas). El EntityGraph trae persona y su familia
 * en la misma consulta para poder armar el DTO sin consultas adicionales (evita N+1).
 */
@Repository
public interface RepositorioRegistroAcceso extends JpaRepository<RegistroAcceso, Long> {

    /** Visita abierta (sin salida) de una persona, si la tiene. Usa el índice idx_acceso_persona_salida. */
    @EntityGraph(attributePaths = { "persona", "persona.grupoFamiliar" })
    Optional<RegistroAcceso> findFirstByPersonaIdAndFechaHoraSalidaIsNull(long personaId);

    /** Personas que están dentro del club en este momento (más recientes primero). */
    @EntityGraph(attributePaths = { "persona", "persona.grupoFamiliar" })
    List<RegistroAcceso> findByFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc();

    /** Últimos 100 movimientos (entradas/salidas) para el historial de recepción. */
    @EntityGraph(attributePaths = { "persona", "persona.grupoFamiliar" })
    List<RegistroAcceso> findTop100ByOrderByFechaHoraEntradaDesc();

    /** Cantidad de personas dentro del club ahora (tablero). */
    long countByFechaHoraSalidaIsNull();

    /** Cantidad de ingresos en un rango de fechas (p. ej. "hoy"). */
    long countByFechaHoraEntradaBetween(LocalDateTime desde, LocalDateTime hasta);
}
