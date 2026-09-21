package com.club.fitnessclub.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.Pago;

/** Repositorio de Pago. El EntityGraph trae cuota y familia para armar el DTO sin N+1. */
@Repository
public interface RepositorioPago extends JpaRepository<Pago, Long> {

    /** Historial de pagos vigentes, del más reciente al más antiguo. */
    @EntityGraph(attributePaths = { "cuota", "cuota.grupoFamiliar" })
    List<Pago> findByActivoTrueOrderByFechaPagoDesc();

    @EntityGraph(attributePaths = { "cuota", "cuota.grupoFamiliar" })
    Optional<Pago> findByIdAndActivoTrue(long id);

    /**
     * Total cobrado en un rango [desde, hasta). Devuelve null si no hubo pagos (SUM sobre cero
     * filas): el servicio lo traduce a cero.
     */
    @Query("select sum(p.monto) from Pago p where p.activo = true and p.fechaPago >= :desde and p.fechaPago < :hasta")
    BigDecimal sumarMontoEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
