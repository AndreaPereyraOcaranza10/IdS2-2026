package com.colegio.sistemaescolar.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO del tablero (dashboard) del panel. Para el ADMIN cuenta todo el colegio;
 * para un DOCENTE cuenta solo lo que tiene asignado.
 */
@Data
public class PanelDTO {

    private boolean admin;

    private long alumnos;
    private long docentes;
    private long grados;
    private long aulas;
    private long materias;
    private long notas;

    /** Promedio general de notas (null si aún no hay notas). */
    private Double promedioGeneral;

    /** Asignaciones del docente, como texto: "2.º Grado - Matemática". */
    private List<String> misAsignaciones = new ArrayList<>();
}
