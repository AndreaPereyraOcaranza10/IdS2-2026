package com.club.fitnessclub.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.UsuarioDTO;
import com.club.fitnessclub.enums.Rol;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.services.ServicioUsuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador del ABM de USUARIOS del sistema (solo ADMIN).
 * Adicionalmente al servicio (que protege al último ADMIN), acá se impide que un usuario se dé de
 * baja a sí mismo: para eso se usa el Principal (usuario autenticado que Spring inyecta).
 */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class ControladorUsuario extends ControladorBase {

    private final ServicioUsuario servicioUsuario;

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("usuarios", servicioUsuario.findAll());
        } catch (Exception e) {
            error(model, e);
        }
        return "usuarios/abm";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioDTO());
        model.addAttribute("roles", Rol.values());
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("usuario", servicioUsuario.findById(id));
            model.addAttribute("roles", Rol.values());
            return "usuarios/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") UsuarioDTO dto, BindingResult resultado,
                          Model model, RedirectAttributes ra) {
        model.addAttribute("roles", Rol.values());
        if (resultado.hasErrors()) {
            return "usuarios/form";
        }
        try {
            if (dto.getId() == 0) {
                servicioUsuario.save(dto);
                ok(ra, "Usuario creado correctamente.");
            } else {
                servicioUsuario.update(dto.getId(), dto);
                ok(ra, "Usuario actualizado correctamente.");
            }
            return "redirect:/usuarios";
        } catch (Exception e) {
            error(model, e);
            return "usuarios/form";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        try {
            if (servicioUsuario.findById(id).getUsername().equals(principal.getName())) {
                throw new ExcepcionNegocio("No puede darse de baja a sí mismo.");
            }
            servicioUsuario.delete(id);
            ok(ra, "Usuario dado de baja correctamente.");
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/usuarios";
    }
}
