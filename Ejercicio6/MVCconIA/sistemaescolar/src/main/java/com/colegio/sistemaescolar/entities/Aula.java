package com.colegio.sistemaescolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * AULA física donde cursan los alumnos (por ejemplo "A-101", capacidad 30).
 * La capacidad se valida al asignar alumnos (ver ServicioAlumno).
 */
@Entity
@Table(name = "aulas",
        uniqueConstraints = @UniqueConstraint(name = "uk_aulas_codigo", columnNames = "codigo"))
@Getter
@Setter
public class Aula extends Auditable {

    /** Identificador visible del aula (único). */
    @Column(nullable = false, length = 20)
    private String codigo;

    /** Cantidad máxima de alumnos. */
    @Column(nullable = false)
    private int capacidad;

    /** Ubicación descriptiva (piso, edificio...). Opcional. */
    @Column(length = 100)
    private String ubicacion;
}
