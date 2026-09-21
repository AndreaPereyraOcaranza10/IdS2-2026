package com.colegio.sistemaescolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * GRADO escolar (por ejemplo "1.º Grado", "2.º Grado"...).
 *
 * <ul>
 *   <li>{@code @Entity}: clase persistente (JPA/Hibernate crea la tabla {@code grados}).</li>
 *   <li>{@code @Table}: nombre de la tabla y restricción de unicidad del nombre.</li>
 *   <li>{@code nivel}: número ordinal (1..12) usado para ordenar los grados.</li>
 * </ul>
 * Relaciones: un Grado tiene muchos Alumnos (lado dueño en {@link Alumno}) y muchos Docentes
 * (tabla intermedia definida en {@link Docente}).
 */
@Entity
@Table(name = "grados",
        uniqueConstraints = @UniqueConstraint(name = "uk_grados_nombre", columnNames = "nombre"))
@Getter
@Setter
public class Grado extends Auditable {

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false)
    private int nivel;
}
