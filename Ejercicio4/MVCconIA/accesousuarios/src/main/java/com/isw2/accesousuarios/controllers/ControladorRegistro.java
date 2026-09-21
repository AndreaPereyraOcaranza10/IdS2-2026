package com.isw2.accesousuarios.controllers;

import com.isw2.accesousuarios.dtos.RegistroDTO;
import com.isw2.accesousuarios.exceptions.ExcepcionAcceso;
import com.isw2.accesousuarios.services.ServicioUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CAPA: CONTROLADOR (MVC) - registro de personas
 * ============================================================================
 * RUTAS (todas bajo /registro)
 *   GET  /registro -> muestra el formulario (puede llegar con el correo ya
 *                     cargado si viene redirigido desde un ingreso fallido).
 *   POST /registro -> valida el formato, delega el alta en el servicio y:
 *                       ok    -> redirige a /login con mensaje de exito
 *                       error -> vuelve al formulario con el error junto al campo
 *
 * ANOTACIONES DE CLASE
 *   @Controller     : bean de la capa web (ver ControladorAutenticacion).
 *   @RequestMapping("/registro"): prefijo comun de las rutas de la clase; los
 *                     @GetMapping/@PostMapping sin path responden a /registro.
 *   @RequiredArgsConstructor: inyección por constructor.
 */
@Controller
@RequestMapping("/registro")
@RequiredArgsConstructor
public class ControladorRegistro {

    private final ServicioUsuario servicioUsuario;

    /**
     * Muestra el formulario. Si el modelo ya trae un "registro" (flash attribute
     * enviado por ControladorAutenticacion con el correo precargado) se respeta;
     * si no, se crea un DTO vacio.
     */
    @GetMapping
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("registro")) {
            model.addAttribute("registro", new RegistroDTO());
        }
        return "registro";
    }

    /**
     * Procesa el alta.
     *
     * Dos niveles de control:
     *   1) Formato (@Valid + BindingResult): campos vacios, correo mal escrito,
     *      fecha futura, etc.
     *   2) Negocio (ServicioUsuario): claves distintas, correo o documento ya
     *      registrados. El motivo de ExcepcionAcceso permite asociar el error al
     *      campo correcto con rejectValue; el resto se muestra como error general.
     *
     * Si todo sale bien se aplica Post-Redirect-Get: se redirige a /login con un
     * flash attribute, evitando que un F5 reenvíe el formulario.
     */
    @PostMapping
    public String procesarRegistro(@Valid @ModelAttribute("registro") RegistroDTO registro,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            servicioUsuario.registrar(registro);
            redirectAttributes.addFlashAttribute("registroExitoso",
                    "Registro exitoso. Ya puede ingresar con su correo y su clave.");
            return "redirect:/login";

        } catch (ExcepcionAcceso e) {
            switch (e.getMotivo()) {
                case CLAVES_NO_COINCIDEN -> bindingResult.rejectValue("confirmarClave", "registro.clave", e.getMessage());
                case CORREO_DUPLICADO -> bindingResult.rejectValue("correo", "registro.correo", e.getMessage());
                case DOCUMENTO_DUPLICADO -> bindingResult.rejectValue("documento", "registro.documento", e.getMessage());
                default -> bindingResult.reject("registro.error", e.getMessage());
            }
            return "registro";

        } catch (Exception e) {
            bindingResult.reject("registro.error", "No se pudo completar el registro. Intente nuevamente.");
            return "registro";
        }
    }
}
