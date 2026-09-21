package com.colegio.sistemaescolar.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

/**
 * Implementación de {@link EdadValida}: calcula los años cumplidos a la fecha de hoy y verifica el rango.
 * Spring/Hibernate Validator la instancia solos cuando se ejecuta {@code @Valid} sobre el DTO.
 */
public class ValidadorEdad implements ConstraintValidator<EdadValida, LocalDate> {

    private int min;
    private int max;

    @Override
    public void initialize(EdadValida anotacion) {
        this.min = anotacion.min();
        this.max = anotacion.max();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext contexto) {
        return esValida(fechaNacimiento, LocalDate.now());
    }

    /** Separado de {@code isValid} para poder probarlo con una fecha "de hoy" fija. */
    boolean esValida(LocalDate fechaNacimiento, LocalDate hoy) {
        if (fechaNacimiento == null) {
            return true;   // la obligatoriedad la controla @NotNull
        }
        if (fechaNacimiento.isAfter(hoy)) {
            return false;  // nacer en el futuro nunca es válido
        }
        int anios = Period.between(fechaNacimiento, hoy).getYears();
        return anios >= min && anios <= max;
    }
}
