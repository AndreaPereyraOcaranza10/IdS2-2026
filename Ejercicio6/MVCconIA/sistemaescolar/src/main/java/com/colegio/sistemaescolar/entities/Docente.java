package com.colegio.sistemaescolar.entities;

import com.colegio.sistemaescolar.enums.Sexo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DOCENTE (profesor) del colegio.
 *
 * <p>Datos de registro pedidos: Nombre, Apellido, Sexo y Fecha de Nacimiento. El correo personal
 * (que es su usuario de acceso) vive en {@link Usuario}, para tener una única fuente de verdad.</p>
 *
 * <h2>Relaciones</h2>
 * <ul>
 *   <li>{@code @OneToOne} Usuario: credenciales de acceso (un docente = un usuario).</li>
 * </ul>
 * Qué dicta y dónde NO se guarda acá: lo define {@link AsignacionDocente} (docente + grado + materia), que determina
 * qué alumnos ve el docente y en qué materia y grado puede cargar notas.
 */
@Entity
@Table(name = "docentes")
@Getter
@Setter
public class Docente extends Auditable {

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Sexo sexo;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /** unique = true en la FK garantiza la relación 1 a 1 a nivel de base de datos. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

}
