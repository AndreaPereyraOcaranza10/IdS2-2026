package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.MateriaDTO;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioMateria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CONTROLADOR ABM (Alta / Baja / Modificación) de materias. Solo accesible para el rol ADMIN
 * (regla {@code /panel/materias/**} en {@code ConfiguracionSeguridad}).
 *
 * <h2>Rutas</h2>
 * <pre>
 * GET  /panel/materias              lista
 * GET  /panel/materias/nuevo        formulario vacío
 * POST /panel/materias              crea
 * GET  /panel/materias/{id}/editar  formulario con datos
 * POST /panel/materias/{id}         actualiza
 * POST /panel/materias/{id}/eliminar  baja lógica
 * </pre>
 * (Se usa POST para modificar/eliminar porque los formularios HTML solo soportan GET y POST, y así se aprovecha el token CSRF.)
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @RequestMapping}: prefijo común de todas las rutas de la clase.</li>
 *   <li>{@code @PathVariable}: toma el {@code {id}} de la URL.</li>
 *   <li>{@code @Valid @ModelAttribute}: enlaza el formulario al DTO y ejecuta sus validaciones.</li>
 *   <li>{@code RedirectAttributes.addFlashAttribute}: mensaje que sobrevive a UN redirect (patrón PRG).</li>
 * </ul>
 */
@Controller
@RequestMapping("/panel/materias")
@RequiredArgsConstructor
public class MateriaControlador {

    private static final String VISTA_FORMULARIO = "materias/formulario";

    private final ServicioMateria servicio;

    @GetMapping
    public String lista(Model modelo) throws Exception {
        modelo.addAttribute("materias", servicio.listar());
        modelo.addAttribute("menuActivo", "materias");
        return "materias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("materia", new MateriaDTO());
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("materia") MateriaDTO dto,
                        BindingResult resultado,
                        Model modelo,
                        RedirectAttributes atributos) throws Exception {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(dto);
                atributos.addFlashAttribute("exito", "La materia se creó correctamente.");
                return "redirect:/panel/materias";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model modelo) throws Exception {
        modelo.addAttribute("materia", servicio.buscarPorId(id));
        prepararFormulario(modelo, true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id,
                             @Valid @ModelAttribute("materia") MateriaDTO dto,
                             BindingResult resultado,
                             Model modelo,
                             RedirectAttributes atributos) throws Exception {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, dto);
                atributos.addFlashAttribute("exito", "La materia se actualizó correctamente.");
                return "redirect:/panel/materias";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        dto.setId(id);
        prepararFormulario(modelo, true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) throws Exception {
        try {
            servicio.eliminar(id);
            atributos.addFlashAttribute("exito", "La materia se eliminó correctamente.");
        } catch (ExcepcionNegocio e) {
            // Regla de integridad (p. ej. tiene alumnos asignados): se informa sin perder la pantalla.
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panel/materias";
    }

    private void prepararFormulario(Model modelo, boolean edicion) {
        modelo.addAttribute("edicion", edicion);
        modelo.addAttribute("menuActivo", "materias");
    }
}
