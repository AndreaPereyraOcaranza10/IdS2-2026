package com.colegio.sistemaescolar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricción de validación propia: la edad calculada a partir de una fecha de nacimiento debe estar entre
 * {@link #min()} y {@link #max()} años (inclusive).
 *
 * <p>Se usa sobre campos {@code LocalDate}. Ejemplo: {@code @EdadValida(min = 18)} exige mayoría de edad.
 * El mínimo evita menores; el máximo evita fechas absurdas (p. ej. el año 1800). Un valor nulo se considera válido:
 * la obligatoriedad se controla con {@code @NotNull}, así cada regla informa su propio error.</p>
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @Constraint(validatedBy = ...)}: indica qué clase implementa la regla ({@link ValidadorEdad}).</li>
 *   <li>{@code @Target(FIELD)}: solo se puede poner en atributos.</li>
 *   <li>{@code @Retention(RUNTIME)}: debe existir en tiempo de ejecución para que Spring la lea.</li>
 * </ul>
 */
@Documented
@Constraint(validatedBy = ValidadorEdad.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EdadValida {

    /** Edad mínima en años cumplidos (inclusive). */
    int min() default 0;

    /** Edad máxima en años cumplidos (inclusive). */
    int max() default 120;

    String message() default "La edad debe estar entre {min} y {max} años (según la fecha de nacimiento).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
