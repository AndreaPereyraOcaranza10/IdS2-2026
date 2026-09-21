package com.colegio.sistemaescolar.validation;

import com.colegio.sistemaescolar.dtos.RegistroDocenteDTO;
import com.colegio.sistemaescolar.enums.Sexo;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests de la regla de edad (mayoría de edad del docente) con una fecha "de hoy" fija. */
class ValidadorEdadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 20);
    private static ValidadorEdad validador;

    @BeforeAll
    static void preparar() {
        validador = new ValidadorEdad();
        // Se configura igual que lo haría Spring a partir de @EdadValida(min = 18, max = 100)
        validador.initialize(new EdadValida() {
            public int min() { return 18; }
            public int max() { return 100; }
            public String message() { return ""; }
            public Class<?>[] groups() { return new Class<?>[0]; }
            @SuppressWarnings("unchecked")
            public Class<? extends jakarta.validation.Payload>[] payload() { return new Class[0]; }
            public Class<? extends java.lang.annotation.Annotation> annotationType() { return EdadValida.class; }
        });
    }

    @Test
    void cumplirDieciochoHoyEsValido() {
        assertThat(validador.esValida(HOY.minusYears(18), HOY)).isTrue();
    }

    @Test
    void fallarPorUnDiaParaLosDieciochoEsInvalido() {
        assertThat(validador.esValida(HOY.minusYears(18).plusDays(1), HOY)).isFalse();
    }

    @Test
    void menorFuturoYAbsurdoSonInvalidos() {
        assertThat(validador.esValida(HOY.minusYears(10), HOY)).isFalse();
        assertThat(validador.esValida(HOY.plusDays(1), HOY)).isFalse();
        assertThat(validador.esValida(LocalDate.of(1800, 1, 1), HOY)).isFalse();
    }

    @Test
    void nuloLoControlaNotNull() {
        assertThat(validador.esValida(null, HOY)).isTrue();
    }

    @Test
    void integradoConBeanValidationRechazaMenoresDeEdad() {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        RegistroDocenteDTO dto = new RegistroDocenteDTO();
        dto.setNombre("Ana");
        dto.setApellido("Ramírez");
        dto.setSexo(Sexo.FEMENINO);
        dto.setEmail("ana@correo.com");
        dto.setPassword("Clave1234");
        dto.setConfirmarPassword("Clave1234");
        dto.setFechaNacimiento(LocalDate.now().minusYears(15));

        assertThat(v.validate(dto)).anyMatch(e -> e.getPropertyPath().toString().equals("fechaNacimiento"));

        dto.setFechaNacimiento(LocalDate.now().minusYears(30));
        assertThat(v.validate(dto)).noneMatch(e -> e.getPropertyPath().toString().equals("fechaNacimiento"));
    }
}
