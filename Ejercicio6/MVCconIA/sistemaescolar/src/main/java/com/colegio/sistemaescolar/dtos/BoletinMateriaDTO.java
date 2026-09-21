package com.colegio.sistemaescolar.dtos;

import com.colegio.sistemaescolar.enums.Periodo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/** Una fila del boletín: promedio por período y promedio final de una materia. */
@Data
public class BoletinMateriaDTO {

    private String materia;

    /** Promedio de las notas de cada período (solo tiene clave si hay notas en ese período). */
    private Map<Periodo, BigDecimal> promedios = new EnumMap<>(Periodo.class);

    private BigDecimal promedio;

    private boolean aprobada;
}
