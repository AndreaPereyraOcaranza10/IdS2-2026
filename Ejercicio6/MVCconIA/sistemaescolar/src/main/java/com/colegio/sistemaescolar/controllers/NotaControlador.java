package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.NotaDTO;
import com.colegio.sistemaescolar.enums.Periodo;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioNota;
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

import java.time.LocalDate;

/**
 * CONTROLADOR de Notas por materia y del boletín del alumno. Accesible para ADMIN y DOCENTE.
 *
 * <p>Los desplegables de alumno y materia se llenan con {@code servicio.alumnosDisponibles()} y
 * {@code servicio.materiasDisponibles()}: el propio servicio filtra según el usuario autenticado, así que un docente
 * solo ve sus alumnos y sus materias. Aunque manipulara el formulario, el servicio vuelve a verificar el permiso.</p>
 */
@Controller
@RequestMapping("/panel/notas")
@RequiredArgsConstructor
public class NotaControlador {

    private static final String VISTA_FORMULARIO = "notas/formulario";

    private final ServicioNota servicio;

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("notas", servicio.listar());
        modelo.addAttribute("menuActivo", "notas");
        return "notas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        NotaDTO dto = new NotaDTO();
        dto.setFecha(LocalDate.now());
        modelo.addAttribute("nota", dto);
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("nota") NotaDTO dto,
                        BindingResult resultado,
                        Model modelo,
                        RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(dto);
                atributos.addFlashAttribute("exito", "La nota se registró correctamente.");
                return "redirect:/panel/notas";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model modelo) throws ExcepcionNegocio {
        modelo.addAttribute("nota", servicio.buscarPorId(id));
        prepararFormulario(modelo, true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id,
                             @Valid @ModelAttribute("nota") NotaDTO dto,
                             BindingResult resultado,
                             Model modelo,
                             RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, dto);
                atributos.addFlashAttribute("exito", "La nota se actualizó correctamente.");
                return "redirect:/panel/notas";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        dto.setId(id);
        prepararFormulario(modelo, true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) throws ExcepcionNegocio {
        servicio.eliminar(id);
        atributos.addFlashAttribute("exito", "La nota se eliminó correctamente.");
        return "redirect:/panel/notas";
    }

    /** Boletín: notas agrupadas por materia y período con promedios (ver {@code ServicioNota.boletin}). */
    @GetMapping("/boletin/{alumnoId}")
    public String boletin(@PathVariable long alumnoId, Model modelo) throws ExcepcionNegocio {
        modelo.addAttribute("boletin", servicio.boletin(alumnoId));
        modelo.addAttribute("periodos", Periodo.values());
        modelo.addAttribute("menuActivo", "alumnos");
        return "notas/boletin";
    }

    private void prepararFormulario(Model modelo, boolean edicion) {
        modelo.addAttribute("edicion", edicion);
        modelo.addAttribute("alumnos", servicio.alumnosDisponibles());
        modelo.addAttribute("materias", servicio.materiasDisponibles());
        modelo.addAttribute("periodos", Periodo.values());
        modelo.addAttribute("menuActivo", "notas");
    }
}
