package com.colegio.sistemaescolar.exceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manejo CENTRALIZADO de errores de negocio que un controlador no capturó.
 *
 * <p>{@code @ControllerAdvice} aplica este manejador a todos los controladores. Si un servicio lanza
 * {@link ExcepcionNegocio} en una acción "simple" (por ejemplo eliminar un registro en uso), el
 * usuario vuelve al panel con un mensaje de error en lugar de ver una pantalla de error técnico.</p>
 *
 * <p>Nota de diseño: NO se captura {@code Exception} genérica a propósito, para no interferir con
 * las excepciones de seguridad ({@code AccessDeniedException}), que Spring Security debe resolver
 * (respuesta 403). Los demás errores inesperados llegan a las páginas {@code templates/error/*}.</p>
 */
@ControllerAdvice(annotations = Controller.class)
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ExcepcionNegocio.class)
    public String manejarNegocio(ExcepcionNegocio excepcion, RedirectAttributes atributos) {
        atributos.addFlashAttribute("error", excepcion.getMessage());
        return "redirect:/panel";
    }
}
