package com.club.fitnessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.GrupoFamiliarDTO;
import com.club.fitnessclub.services.ServicioCuota;
import com.club.fitnessclub.services.ServicioGrupoFamiliar;
import com.club.fitnessclub.services.ServicioPersona;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador del ABM de FAMILIAS (solo ADMIN).
 *
 * <pre>
 *   GET  /familias              -> familias/abm      (listado)
 *   GET  /familias/nueva        -> familias/form     (formulario de alta)
 *   GET  /familias/{id}         -> familias/detalle  (integrantes y cuotas de la familia)
 *   GET  /familias/{id}/editar  -> familias/form     (formulario de edición)
 *   POST /familias/guardar      -> alta o modificación (según id == 0) y redirect al listado
 *   POST /familias/{id}/eliminar-> baja lógica
 * </pre>
 *
 * <p>Capa CONTROLADOR: solo recibe la petición, delega en el servicio y elige la vista. No contiene
 * reglas de negocio ni maneja entidades: trabaja únicamente con DTOs.
 */
@Controller
@RequestMapping("/familias")
@RequiredArgsConstructor
public class ControladorGrupoFamiliar extends ControladorBase {

    private final ServicioGrupoFamiliar servicioGrupo;
    private final ServicioPersona servicioPersona;
    private final ServicioCuota servicioCuota;

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("familias", servicioGrupo.findAll());
        } catch (Exception e) {
            error(model, e);
        }
        return "familias/abm";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("familia", new GrupoFamiliarDTO());
        return "familias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("familia", servicioGrupo.findById(id));
            return "familias/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/familias";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("familia", servicioGrupo.findById(id));
            model.addAttribute("integrantes", servicioPersona.findByGrupoFamiliar(id));
            model.addAttribute("cuotas", servicioCuota.findByGrupoFamiliar(id));
            return "familias/detalle";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/familias";
        }
    }

    /**
     * Alta o modificación.
     * <ul>
     *   <li>@Valid ejecuta las validaciones del DTO (Bean Validation); los errores quedan en BindingResult.</li>
     *   <li>@ModelAttribute("familia") enlaza los campos del formulario con el DTO (data binding).</li>
     * </ul>
     * Si hay errores se vuelve a mostrar el formulario (con los datos tipeados y los mensajes).
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("familia") GrupoFamiliarDTO dto, BindingResult resultado,
                          Model model, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            return "familias/form";
        }
        try {
            if (dto.getId() == 0) {
                servicioGrupo.save(dto);
                ok(ra, "Familia registrada correctamente.");
            } else {
                servicioGrupo.update(dto.getId(), dto);
                ok(ra, "Familia actualizada correctamente.");
            }
            return "redirect:/familias";
        } catch (Exception e) {
            error(model, e);
            return "familias/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes ra) {
        try {
            servicioGrupo.delete(id);
            ok(ra, "Familia dada de baja correctamente.");
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/familias";
    }
}
