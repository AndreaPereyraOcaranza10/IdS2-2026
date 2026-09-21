package com.colegio.sistemaescolar.entities;

import com.colegio.sistemaescolar.enums.Periodo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * NOTA de un alumno en una materia y período.
 *
 * <ul>
 *   <li>Escala 1 a 10 con hasta dos decimales ({@code BigDecimal} evita errores de redondeo de double);
 *       se considera aprobada desde 6.</li>
 *   <li>Un alumno puede tener varias notas por materia y período (parciales, trabajos prácticos...).</li>
 *   <li>Quién cargó la nota queda en los campos de auditoría heredados ({@code creadoPor},
 *       {@code modificadoPor}), por eso no hace falta una relación extra con el docente.</li>
 * </ul>
 */
@Entity
@Table(name = "notas")
@Getter
@Setter
public class Nota extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Periodo periodo;

    /** precision = 4, scale = 2  ->  hasta "10.00". */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal valor;

    /** Fecha de la evaluación. */
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 255)
    private String observaciones;
}
