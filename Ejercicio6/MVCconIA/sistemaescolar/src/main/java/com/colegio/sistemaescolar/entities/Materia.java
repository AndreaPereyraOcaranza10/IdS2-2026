package com.colegio.sistemaescolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * MATERIA que se dicta en el colegio (Lengua, Matemática...). Las notas se registran por materia.
 */
@Entity
@Table(name = "materias",
        uniqueConstraints = @UniqueConstraint(name = "uk_materias_nombre", columnNames = "nombre"))
@Getter
@Setter
public class Materia extends Auditable {

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;
}
