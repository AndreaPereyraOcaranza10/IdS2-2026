package com.techstore.inventario.controllers;

import com.techstore.inventario.dto.ProveedorFormDTO;
import com.techstore.inventario.dto.ProveedorViewDTO;
import com.techstore.inventario.entities.Proveedor;
import com.techstore.inventario.mapper.ProveedorMapper;
import com.techstore.inventario.services.ServicioProveedor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/** Controller de Proveedores: listado, alta, edicion y baja. */
@Controller
@RequestMapping("/proveedores")
public class ControladorProveedor {

    @Autowired
    private ServicioProveedor servicioProveedor;

    @GetMapping
    public String listar(Model model) throws Exception {
        List<ProveedorViewDTO> proveedores = servicioProveedor.findAll().stream()
                .map(ProveedorMapper::aViewDTO)
                .collect(Collectors.toList());
        model.addAttribute("proveedores", proveedores);
        return "views/proveedor/abm";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("proveedorForm", new ProveedorFormDTO());
        return "views/proveedor/formulario/form";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) throws Exception {
        Proveedor proveedor = servicioProveedor.findById(id);
        model.addAttribute("proveedorForm", ProveedorMapper.aFormDTO(proveedor));
        return "views/proveedor/formulario/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("proveedorForm") ProveedorFormDTO form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            return "views/proveedor/formulario/form";
        }

        try {
            if (form.getId() == null) {
                Proveedor nuevo = ProveedorMapper.aEntidadNueva(form);
                servicioProveedor.saveOne(nuevo);
                redirectAttributes.addFlashAttribute("mensaje", "Proveedor creado correctamente.");
            } else {
                Proveedor datosEditados = ProveedorMapper.aEntidadParaUpdate(form);
                servicioProveedor.updateOne(datosEditados, form.getId());
                redirectAttributes.addFlashAttribute("mensaje", "Proveedor actualizado correctamente.");
            }
            return "redirect:/proveedores";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return form.getId() == null ? "redirect:/proveedores/nuevo" : "redirect:/proveedores/" + form.getId() + "/editar";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) throws Exception {
        Proveedor proveedor = servicioProveedor.findById(id);
        model.addAttribute("proveedor", ProveedorMapper.aViewDTO(proveedor));
        return "views/proveedor/detalle";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioProveedor.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Proveedor eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }
}
