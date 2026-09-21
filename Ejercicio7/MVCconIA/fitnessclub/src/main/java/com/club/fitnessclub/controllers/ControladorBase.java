package com.club.fitnessclub.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.exceptions.ExcepcionNegocio;

/**
 * Clase base de los controladores: centraliza el manejo de mensajes para la vista.
 *
 * <p>Convención de atributos que muestra el fragmento "alertas" de las vistas:
 * <ul>
 *   <li>mensajeOk: operación exitosa (verde).</li>
 *   <li>mensajeError: error de negocio o del sistema (rojo).</li>
 *   <li>mensajeAdvertencia: aviso no bloqueante (amarillo).</li>
 * </ul>
 * Tras un REDIRECT se usan atributos FLASH (RedirectAttributes): sobreviven a UNA sola petición
 * y evitan que un F5 reenvíe el formulario (patrón Post/Redirect/Get).
 */
public abstract class ControladorBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Traduce una excepción en un mensaje para el usuario: las de negocio muestran su mensaje
     * (pensado para personas); cualquier otra se registra en el log y se muestra un mensaje genérico
     * (nunca se exponen detalles internos como SQL o stack traces).
     */
    protected String mensajeDe(Exception e) {
        if (e instanceof ExcepcionNegocio) {
            return e.getMessage();
        }
        log.error("Error inesperado", e);
        return "Ocurrió un error inesperado. Intente nuevamente o contacte al administrador.";
    }

    protected void ok(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("mensajeOk", mensaje);
    }

    protected void error(RedirectAttributes ra, Exception e) {
        ra.addFlashAttribute("mensajeError", mensajeDe(e));
    }

    /** Para páginas que se renderizan directamente (sin redirect) tras un error. */
    protected void error(Model model, Exception e) {
        model.addAttribute("mensajeError", mensajeDe(e));
    }

    /** Une los mensajes de validación de un BindingResult en una sola línea (para mostrarlos tras un redirect). */
    protected String erroresDe(BindingResult resultado) {
        StringBuilder sb = new StringBuilder();
        for (FieldError fe : resultado.getFieldErrors()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(fe.getDefaultMessage());
        }
        return sb.toString();
    }
}
