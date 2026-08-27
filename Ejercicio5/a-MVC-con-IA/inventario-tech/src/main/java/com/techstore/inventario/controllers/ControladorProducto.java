package com.techstore.inventario.controllers;

import com.techstore.inventario.dto.ProductoFormDTO;
import com.techstore.inventario.dto.ProductoViewDTO;
import com.techstore.inventario.entities.Producto;
import com.techstore.inventario.mapper.ProductoMapper;
import com.techstore.inventario.services.ServicioProducto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/** Controller de Productos: listado, alta, edicion y baja. */
@Controller
@RequestMapping("/productos")
public class ControladorProducto {

    @Autowired
    private ServicioProducto servicioProducto;

    /** Listado general (vista "abm": alta/baja/modificacion desde una tabla). */
    @GetMapping
    public String listar(Model model) throws Exception {
        List<ProductoViewDTO> productos = servicioProducto.findAll().stream()
                .map(ProductoMapper::aViewDTO)
                .collect(Collectors.toList());
        model.addAttribute("productos", productos);
        return "views/producto/abm";
    }

    /** Formulario de alta (vacio). */
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("productoForm", new ProductoFormDTO());
        return "views/producto/formulario/form";
    }

    /** Formulario de edicion, precargado con los datos actuales. */
    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) throws Exception {
        Producto producto = servicioProducto.findById(id);
        model.addAttribute("productoForm", ProductoMapper.aFormDTO(producto));
        return "views/producto/formulario/form";
    }

    /** Procesa tanto alta como edicion: si el DTO trae id, es edicion. */
    @PostMapping
    public String guardar(@Valid @ModelAttribute("productoForm") ProductoFormDTO form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            return "views/producto/formulario/form";
        }

        try {
            if (form.getId() == null) {
                Producto nuevo = ProductoMapper.aEntidadNueva(form);
                servicioProducto.saveOne(nuevo);
                redirectAttributes.addFlashAttribute("mensaje", "Producto creado correctamente.");
            } else {
                Producto datosEditados = ProductoMapper.aEntidadParaUpdate(form);
                servicioProducto.updateOne(datosEditados, form.getId());
                redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
            }
            return "redirect:/productos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return form.getId() == null ? "redirect:/productos/nuevo" : "redirect:/productos/" + form.getId() + "/editar";
        }
    }

    /** Vista de detalle de un producto puntual. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) throws Exception {
        Producto producto = servicioProducto.findById(id);
        model.addAttribute("producto", ProductoMapper.aViewDTO(producto));
        return "views/producto/detalle";
    }

    /** Baja de un producto. */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioProducto.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }
}
