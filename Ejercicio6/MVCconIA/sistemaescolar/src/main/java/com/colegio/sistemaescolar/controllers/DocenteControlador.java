package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.DocenteDTO;
import com.colegio.sistemaescolar.dtos.RegistroDocenteDTO;
import com.colegio.sistemaescolar.enums.Sexo;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioDocente;
import com.colegio.sistemaescolar.services.ServicioGrado;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CONTROLADOR de administración de Docentes (solo ADMIN).
 *
 * <ul>
 *   <li>Alta por el ADMIN: usa el mismo {@code RegistroDocenteDTO} y el mismo servicio que el autoregistro público,
 *       por eso también dispara el correo de bienvenida.</li>
 *   <li>Edición: datos personales + asignación de grados y materias (checkboxes). Estas asignaciones definen
 *       qué alumnos y notas puede ver/cargar el docente.</li>
 *   <li>Habilitar/deshabilitar: controla si el docente puede iniciar sesión (útil con {@code colegio.registro.requiere-aprobacion=true}).</li>
 * </ul>
 */
@Controller
@RequestMapping("/panel/docentes")
@RequiredArgsConstructor
public class DocenteControlador {

    private final ServicioDocente servicio;
    private final ServicioGrado servicioGrado;
    private final ServicioMateria servicioMateria;

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("docentes", servicio.listar());
        modelo.addAttribute("menuActivo", "docentes");
        return "docentes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("registro", new RegistroDocenteDTO());
        modelo.addAttribute("sexos", Sexo.values());
        modelo.addAttribute("menuActivo", "docentes");
        return "docentes/alta";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("registro") RegistroDocenteDTO registro,
                        BindingResult resultado,
                        Model modelo,
                        RedirectAttributes atributos) {
        if (!resultado.hasErrors()) {
            try {
                servicio.registrar(registro);
                atributos.addFlashAttribute("exito",
                        "El docente se registró y se le envió el correo de bienvenida. Asignale grados y materias.");
                return "redirect:/panel/docentes";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        registro.setPassword(null);
        registro.setConfirmarPassword(null);
        modelo.addAttribute("sexos", Sexo.values());
        modelo.addAttribute("menuActivo", "docentes");
        return "docentes/alta";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model modelo) throws ExcepcionNegocio {
        modelo.addAttribute("docente", servicio.buscarPorId(id));
        prepararEdicion(modelo);
        return "docentes/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable long id,
                             @Valid @ModelAttribute("docente") DocenteDTO dto,
                             BindingResult resultado,
                             Model modelo,
                             RedirectAttributes atributos) throws ExcepcionNegocio {
        if (!resultado.hasErrors()) {
            try {
                servicio.actualizar(id, dto);
                atributos.addFlashAttribute("exito", "El docente se actualizó correctamente.");
                return "redirect:/panel/docentes";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        // El correo es de solo lectura y no viaja en el formulario: se vuelve a leer para mostrarlo.
        dto.setId(id);
        dto.setEmail(servicio.buscarPorId(id).getEmail());
        prepararEdicion(modelo);
        return "docentes/formulario";
    }

    @PostMapping("/{id}/habilitar")
    public String habilitar(@PathVariable long id,
                            @RequestParam boolean habilitado,
                            RedirectAttributes atributos) throws ExcepcionNegocio {
        servicio.cambiarHabilitacion(id, habilitado);
        atributos.addFlashAttribute("exito", habilitado ? "Cuenta habilitada." : "Cuenta deshabilitada.");
        return "redirect:/panel/docentes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes atributos) throws ExcepcionNegocio {
        servicio.eliminar(id);
        atributos.addFlashAttribute("exito", "El docente se eliminó correctamente.");
        return "redirect:/panel/docentes";
    }

    private void prepararEdicion(Model modelo) {
        modelo.addAttribute("grados", servicioGrado.listar());
        modelo.addAttribute("materias", servicioMateria.listar());
        modelo.addAttribute("sexos", Sexo.values());
        modelo.addAttribute("menuActivo", "docentes");
    }
}
