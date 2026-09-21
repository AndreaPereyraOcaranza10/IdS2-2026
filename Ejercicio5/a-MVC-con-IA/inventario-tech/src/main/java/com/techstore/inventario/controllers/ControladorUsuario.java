package com.techstore.inventario.controllers;

import com.techstore.inventario.dto.UsuarioFormDTO;
import com.techstore.inventario.dto.UsuarioViewDTO;
import com.techstore.inventario.entities.Usuario;
import com.techstore.inventario.mapper.UsuarioMapper;
import com.techstore.inventario.services.ServicioUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/** Controller de Usuarios: listado, alta, edicion y baja. */
@Controller
@RequestMapping("/usuarios")
public class ControladorUsuario {

    @Autowired
    private ServicioUsuario servicioUsuario;

    @GetMapping
    public String listar(Model model) throws Exception {
        List<UsuarioViewDTO> usuarios = servicioUsuario.findAll().stream()
                .map(UsuarioMapper::aViewDTO)
                .collect(Collectors.toList());
        model.addAttribute("usuarios", usuarios);
        return "views/usuario/abm";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioFormDTO());
        return "views/usuario/formulario/form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) throws Exception {
        Usuario usuario = servicioUsuario.findById(id);
        model.addAttribute("usuarioForm", UsuarioMapper.aFormDTO(usuario));
        return "views/usuario/formulario/form";
    }

    /**
     * Procesa alta y edicion. La validacion de password es condicional (obligatoria
     * solo en alta), por eso se hace a mano aca en vez de con una anotacion en el DTO.
     */
    @PostMapping
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioFormDTO form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) throws Exception {

        if (form.getId() == null && (form.getPassword() == null || form.getPassword().isBlank())) {
            bindingResult.rejectValue("password", "NotBlank", "La contrasenia es obligatoria para un usuario nuevo");
        }

        if (bindingResult.hasErrors()) {
            return "views/usuario/formulario/form";
        }

        try {
            if (form.getId() == null) {
                Usuario nuevo = UsuarioMapper.aEntidadNueva(form);
                servicioUsuario.saveOne(nuevo);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente.");
            } else {
                Usuario datosEditados = UsuarioMapper.aEntidadParaUpdate(form);
                servicioUsuario.updateOne(datosEditados, form.getId());
                redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
            }
            return "redirect:/usuarios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return form.getId() == null ? "redirect:/usuarios/nuevo" : "redirect:/usuarios/" + form.getId() + "/editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioUsuario.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
