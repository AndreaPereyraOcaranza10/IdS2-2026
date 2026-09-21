package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.config.PropiedadesColegio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * Datos comunes a TODAS las vistas.
 *
 * <p>{@code @ControllerAdvice} + {@code @ModelAttribute}: Spring ejecuta estos métodos antes de cada controlador y agrega
 * su resultado al {@code Model}. Así las plantillas pueden usar {@code ${colegio.nombre}} o {@code ${anioActual}} en el
 * encabezado y el pie sin que cada controlador tenga que repetirlo.</p>
 */
@ControllerAdvice
@RequiredArgsConstructor
public class AtributosGlobales {

    private final PropiedadesColegio propiedades;

    @ModelAttribute("colegio")
    public PropiedadesColegio colegio() {
        return propiedades;
    }

    @ModelAttribute("anioActual")
    public int anioActual() {
        return LocalDate.now().getYear();
    }
}
