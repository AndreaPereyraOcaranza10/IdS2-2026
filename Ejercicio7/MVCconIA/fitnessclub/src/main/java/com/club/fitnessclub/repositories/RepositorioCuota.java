package com.club.fitnessclub.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.Cuota;
import com.club.fitnessclub.enums.EstadoCuota;

/**
 * Repositorio de Cuota. Las consultas con JOIN FETCH cargan la familia y los pagos en UNA sola
 * consulta, porque el mapeador calcula montoPagado/saldo y muestra el nombre de la familia
 * (relaciones LAZY, y con open-in-view=false no se pueden leer fuera de la transacción).
 */
@Repository
public interface RepositorioCuota extends JpaRepository<Cuota, Long> {

    /** Todas las cuotas vigentes con familia y pagos, de la más reciente a la más antigua. */
    @Query("select distinct c from Cuota c join fetch c.grupoFamiliar left join fetch c.pagos "
            + "where c.activo = true order by c.periodo desc, c.id desc")
    List<Cuota> findAllConDetalle();

    /** Una cuota con su familia y sus pagos. */
    @Query("select c from Cuota c join fetch c.grupoFamiliar left join fetch c.pagos "
            + "where c.id = :id and c.activo = true")
    Optional<Cuota> findByIdConDetalle(@Param("id") long id);

    /** Cuotas de una familia (pantalla de detalle de familia). */
    @Query("select distinct c from Cuota c join fetch c.grupoFamiliar left join fetch c.pagos "
            + "where c.grupoFamiliar.id = :grupoId and c.activo = true order by c.periodo desc")
    List<Cuota> findByGrupoFamiliarConDetalle(@Param("grupoId") long grupoId);

    /** ¿Ya existe una cuota de ese período para la familia? (incluye anuladas: la restricción única de la BD las cuenta). */
    boolean existsByGrupoFamiliarIdAndPeriodo(long grupoFamiliarId, LocalDate periodo);

    /** ¿La familia tiene cuotas pendientes ya vencidas? (advertencia en recepción). */
    boolean existsByGrupoFamiliarIdAndEstadoAndFechaVencimientoBeforeAndActivoTrue(
            long grupoFamiliarId, EstadoCuota estado, LocalDate fecha);

    /** Cantidad de cuotas por estado (tablero). */
    long countByEstadoAndActivoTrue(EstadoCuota estado);

    /** Cantidad de cuotas en un estado con vencimiento anterior a la fecha (cuotas vencidas para el tablero). */
    long countByEstadoAndFechaVencimientoBeforeAndActivoTrue(EstadoCuota estado, LocalDate fecha);

    /** ¿La familia tiene cuotas en ese estado? (impide dar de baja una familia con cuotas pendientes). */
    boolean existsByGrupoFamiliarIdAndEstadoAndActivoTrue(long grupoFamiliarId, EstadoCuota estado);
}
