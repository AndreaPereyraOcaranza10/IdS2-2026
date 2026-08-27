package com.techstore.inventario.controllers;

import com.techstore.inventario.dto.OrdenCompraFormDTO;
import com.techstore.inventario.dto.OrdenCompraViewDTO;
import com.techstore.inventario.entities.*;
import com.techstore.inventario.mapper.OrdenCompraMapper;
import com.techstore.inventario.services.ServicioOrdenCompra;
import com.techstore.inventario.services.ServicioProducto;
import com.techstore.inventario.services.ServicioProveedor;
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

/**
 * Controller de Ordenes de Compra: expone las pantallas de listado, alta
 * (formulario dinamico), detalle, confirmacion y anulacion.
 * Sigue el patron MVC estricto: recibe/devuelve DTOs, nunca entidades.
 */
@Controller
@RequestMapping("/ordenes-compra")
public class ControladorOrdenCompra {

    @Autowired
    private ServicioOrdenCompra servicioOrdenCompra;

    @Autowired
    private ServicioProveedor servicioProveedor;

    @Autowired
    private ServicioProducto servicioProducto;

    @Autowired
    private ServicioUsuario servicioUsuario;

    /** Listado general de ordenes (vista "busqueda"). */
    @GetMapping
    public String listar(Model model) throws Exception {
        List<OrdenCompraViewDTO> ordenes = servicioOrdenCompra.findAll().stream()
                .map(OrdenCompraMapper::aViewDTO)
                .collect(Collectors.toList());
        model.addAttribute("ordenes", ordenes);
        return "views/ordencompra/busqueda";
    }

    /** Muestra el formulario de alta (proveedores y productos para los combos del JS). */
    @GetMapping("/nueva")
    public String formularioNuevo(Model model) throws Exception {
        model.addAttribute("ordenCompraForm", new OrdenCompraFormDTO());
        model.addAttribute("proveedores", servicioProveedor.findAll());
        model.addAttribute("productos", servicioProducto.findAll());
        model.addAttribute("usuarios", servicioUsuario.findAll());
        return "views/ordencompra/formulario/form";
    }

    /**
     * Procesa el alta: valida el DTO, resuelve las entidades reales (Proveedor,
     * Usuario, Productos) y delega en ServicioOrdenCompra.crearBorrador().
     */
    @PostMapping
    public String crear(@Valid @ModelAttribute("ordenCompraForm") OrdenCompraFormDTO form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) throws Exception {

        if (bindingResult.hasErrors()) {
            // Se recargan los combos porque la vista los necesita para volver a renderizar el form.
            model.addAttribute("proveedores", servicioProveedor.findAll());
            model.addAttribute("productos", servicioProducto.findAll());
            model.addAttribute("usuarios", servicioUsuario.findAll());
            return "views/ordencompra/formulario/form";
        }

        try {
            Proveedor proveedor = servicioProveedor.findById(form.getProveedorId());
            Usuario usuario = servicioUsuario.findById(form.getUsuarioId());

            List<DetalleOrdenCompra> detalles = OrdenCompraMapper.aEntidadesDetalle(
                    form.getDetalles(),
                    idProducto -> {
                        try {
                            return servicioProducto.findById(idProducto);
                        } catch (Exception e) {
                            throw new RuntimeException(e); // se traduce mas abajo en el catch general
                        }
                    }
            );

            OrdenCompra creada = servicioOrdenCompra.crearBorrador(proveedor, usuario, detalles);
            redirectAttributes.addFlashAttribute("mensaje", "Orden " + creada.getNumeroOrden() + " creada en estado BORRADOR.");
            return "redirect:/ordenes-compra/" + creada.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ordenes-compra/nueva";
        }
    }

    /** Vista de detalle de una orden puntual (con sus lineas y acciones segun su estado). */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) throws Exception {
        OrdenCompra orden = servicioOrdenCompra.findById(id);
        model.addAttribute("orden", OrdenCompraMapper.aViewDTO(orden));
        return "views/ordencompra/detalle";
    }

    /** Accion de confirmar: dispara el impacto en stock dentro del service. */
    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioOrdenCompra.confirmar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Orden confirmada. Stock actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ordenes-compra/" + id;
    }

    /** Accion de anular: revierte stock si correspondia (ver ServicioOrdenCompra.anular). */
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicioOrdenCompra.anular(id);
            redirectAttributes.addFlashAttribute("mensaje", "Orden anulada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ordenes-compra/" + id;
    }
}
