package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.AlumnoDTO;
import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioAlumno;
import com.colegio.sistemaescolar.services.ServicioAula;
import com.colegio.sistemaescolar.services.ServicioGrado;
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
 * CONTROLADOR de Alumnos (con su grado y aula).
 *
 * <h2>Seguridad</h2>
 * <ul>
 *   <li>Ver la lista y el detalle: ADMIN y DOCENTE. El servicio limita al docente a los alumnos de SUS grados.</li>
 *   <li>Alta, edición y baja: solo ADMIN. Se protege en TRES niveles: (1) {@code ConfiguracionSeguridad} bloquea las
 *       URL, (2) las plantillas ocultan los botones con {@code sec:authorize}, (3) {@code ServicioAlumno.exigirAdmin()}.</li>
 * </ul>
 * Las opciones de los desplegables (grados, aulas, sexo) llegan a la vista como listas de DTO / enum, no como entidades.
 */
@Controller
@RequestMapping("/panel/alumnos")
@RequiredArgsConstructor
public class AlumnoControlador {

    private static final String VISTA_FORMULARIO = "alumnos/formulario";

    private final ServicioAlumno servicio;
    private final ServicioGrado servicioGrado;
    private final ServicioAula servicioAula;

    @GetMapping
    public String lista(Model modelo) throws Exception {
        modelo.addAttribute("alumnos", servicio.listar());
        modelo.addAttribute("menuActivo", "alumnos");
        return "alumnos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model modelo) throws Exception {
        modelo.addAttribute("alumno", servicio.buscarPorId(id));
        modelo.addAttribute("menuActivo", "alumnos");
        return "alumnos/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("alumno", new AlumnoDTO());
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("alumno") AlumnoDTO dto,
                        BindingResult resultado,
                        Model modelo,
                        RedirectAttributes atributos) throws Exception {
        if (!resultado.hasErrors()) {
            try {
                servicio.crear(dto);
                atributos.addFlashAttribute("exito", "El alumno se registró correctamente.");
                return "redirect:/panel/alumnos";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        prepararFormulario(modelo, false);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model modelo) throws Exception {
        modelo.addAttribute("alumno", servicio.buscarPorId(id));
        prepararFormulario(modelo, true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id,
                             @Valid @ModelAttribute("alumno") AlumnoDTO dto,
                             BindingResult resultado,
                             Model modelo,
                             RedirectAttributes atributos) throws Exception {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, dto);
                atributos.addFlashAttribute("exito", "El alumno se actualizó correctamente.");
                return "redirect:/panel/alumnos";
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
            atributos.addFlashAttribute("exito", "El alumno se eliminó correctamente.");
        } catch (ExcepcionNegocio e) {
            atributos.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panel/alumnos";
    }

    private void prepararFormulario(Model modelo, boolean edicion) {
        modelo.addAttribute("edicion", edicion);
        modelo.addAttribute("grados", servicioGrado.listar());
        modelo.addAttribute("aulas", servicioAula.listar());
        modelo.addAttribute("sexos", Sexo.values());
        modelo.addAttribute("menuActivo", "alumnos");
    }
}
