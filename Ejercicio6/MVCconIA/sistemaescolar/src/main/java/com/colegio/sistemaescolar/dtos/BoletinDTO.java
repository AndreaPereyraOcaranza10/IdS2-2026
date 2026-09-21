package com.colegio.sistemaescolar.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Boletín de un alumno: notas agrupadas por materia y período. */
@Data
public class BoletinDTO {

    private long alumnoId;
    private String alumnoNombre;
    private String gradoNombre;
    private String aulaCodigo;

    private List<BoletinMateriaDTO> materias = new ArrayList<>();

    /** Promedio general de todas las materias (null si no hay notas). */
    private BigDecimal promedioGeneral;
}
