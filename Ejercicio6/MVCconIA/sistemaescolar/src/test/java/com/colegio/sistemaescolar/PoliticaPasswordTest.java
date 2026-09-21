package com.colegio.sistemaescolar;

import com.colegio.sistemaescolar.utils.Textos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests de las expresiones regulares y utilidades de texto usadas por las validaciones de los DTO. */
class PoliticaPasswordTest {

    @Test
    void aceptaPasswordConLetraYNumero() {
        assertThat("Clave1234").matches(Textos.REGEX_PASSWORD);
    }

    @Test
    void rechazaPasswordCortaSinNumeroOSinLetra() {
        assertThat("Ab1").doesNotMatch(Textos.REGEX_PASSWORD);
        assertThat("soloLetras").doesNotMatch(Textos.REGEX_PASSWORD);
        assertThat("12345678").doesNotMatch(Textos.REGEX_PASSWORD);
    }

    @Test
    void nombreAceptaAcentosYRechazaNumeros() {
        assertThat("María José").matches(Textos.REGEX_NOMBRE);
        assertThat("O'Connor-Pérez").matches(Textos.REGEX_NOMBRE);
        assertThat("Ana3").doesNotMatch(Textos.REGEX_NOMBRE);
    }

    @Test
    void limpiarRecortaYConvierteVacioEnNulo() {
        assertThat(Textos.limpiar("  hola ")).isEqualTo("hola");
        assertThat(Textos.limpiar("   ")).isNull();
        assertThat(Textos.limpiarNoNulo(null)).isEmpty();
    }
}
