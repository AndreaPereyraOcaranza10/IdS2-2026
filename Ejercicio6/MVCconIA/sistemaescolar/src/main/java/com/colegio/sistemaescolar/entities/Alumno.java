package com.colegio.sistemaescolar.entities;

import com.colegio.sistemaescolar.enums.Sexo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * ALUMNO del colegio, con su GRADO y su AULA.
 *
 * <h2>Relaciones</h2>
 * <ul>
 *   <li>{@code @ManyToOne} Grado: muchos alumnos pertenecen a un grado.</li>
 *   <li>{@code @ManyToOne} Aula: muchos alumnos cursan en un aula.</li>
 *   <li>{@code fetch = LAZY}: el grado/aula se cargan de la base solo cuando se usan
 *       (evita consultas innecesarias). Como {@code open-in-view=false}, se acceden dentro
 *       de la transacción del servicio y se copian al DTO.</li>
 *   <li>{@code optional = false}: la base exige que todo alumno tenga grado y aula.</li>
 * </ul>
 */
@Entity
@Table(name = "alumnos",
        uniqueConstraints = @UniqueConstraint(name = "uk_alumnos_dni", columnNames = "dni"))
@Getter
@Setter
public class Alumno extends Auditable {

    /** Documento de identidad (solo dígitos, 7 u 8). Único. */
    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Sexo sexo;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 150)
    private String domicilio;

    /** Madre, padre o tutor de referencia. */
    @Column(name = "nombre_tutor", length = 120)
    private String nombreTutor;

    @Column(name = "telefono_tutor", length = 30)
    private String telefonoTutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grado_id", nullable = false)
    private Grado grado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    private Aula aula;
}
