package com.colegio.sistemaescolar.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * ASIGNACIÓN DOCENTE: "el docente X dicta la materia Y en el grado Z".
 *
 * <h2>Por qué es una entidad y no dos listas</h2>
 * Con dos relaciones {@code @ManyToMany} independientes (grados por un lado, materias por otro) no se puede expresar
 * que un docente dicta Matemática en 1.º Grado pero NO en 2.º. Esta entidad guarda la combinación exacta, y de ella
 * depende la autorización a nivel de datos: un docente solo ve las notas y califica a un alumno si existe una
 * asignación que coincida con el grado del alumno Y con la materia.
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @UniqueConstraint}: impide asignar dos veces la misma terna docente-grado-materia.</li>
 *   <li>{@code @ManyToOne(fetch = LAZY)}: cada asignación apunta a un docente, un grado y una materia; se cargan
 *       bajo demanda para no traer datos innecesarios.</li>
 * </ul>
 * Hereda de {@link Auditable}: queda registrado quién y cuándo asignó. Las asignaciones se borran físicamente al
 * quitarlas (son configuración, no historial académico); el historial de notas no depende de ellas.
 */
@Entity
@Table(name = "asignaciones_docentes",
        uniqueConstraints = @UniqueConstraint(name = "uk_asignacion_docente_grado_materia",
                columnNames = {"docente_id", "grado_id", "materia_id"}))
@Getter
@Setter
public class AsignacionDocente extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grado_id", nullable = false)
    private Grado grado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;
}
