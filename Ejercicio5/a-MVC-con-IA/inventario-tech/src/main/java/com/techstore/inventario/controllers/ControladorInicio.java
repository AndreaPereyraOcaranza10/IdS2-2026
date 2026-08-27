package com.techstore.inventario.controllers;

import com.techstore.inventario.dto.OrdenCompraViewDTO;
import com.techstore.inventario.dto.ProductoViewDTO;
import com.techstore.inventario.mapper.OrdenCompraMapper;
import com.techstore.inventario.mapper.ProductoMapper;
import com.techstore.inventario.services.ServicioOrdenCompra;
import com.techstore.inventario.services.ServicioProducto;
import com.techstore.inventario.services.ServicioProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard de inicio: contadores generales y dos listas de seguimiento
 * (productos con stock bajo, ordenes de compra recientes). Es solo lectura:
 * agrega datos de los tres services existentes, no tiene logica propia.
 */
@Controller
public class ControladorInicio {

    @Autowired
    private ServicioProducto servicioProducto;

    @Autowired
    private ServicioProveedor servicioProveedor;

    @Autowired
    private ServicioOrdenCompra servicioOrdenCompra;

    @GetMapping("/")
    public String inicio(Model model) throws Exception {
        List<ProductoViewDTO> productos = servicioProducto.findAll().stream()
                .map(ProductoMapper::aViewDTO)
                .collect(Collectors.toList());

        List<OrdenCompraViewDTO> ordenes = servicioOrdenCompra.findAll().stream()
                .map(OrdenCompraMapper::aViewDTO)
                .collect(Collectors.toList());

        long totalProductos = productos.size();
        long totalProveedoresActivos = servicioProveedor.findAll().stream()
                .filter(p -> p.isActivo())
                .count();
        long ordenesEnBorrador = ordenes.stream()
                .filter(o -> "BORRADOR".equals(o.getEstado()))
                .count();

        // Cantidad TOTAL de productos bajo el minimo (para la tarjeta contador),
        // separada de la lista recortada de abajo (que solo muestra los 5 mas urgentes).
        long cantidadStockBajo = productos.stream()
                .filter(ProductoViewDTO::isBajoStockMinimo)
                .count();

        // Top 5 productos con stock por debajo del minimo, para reponer primero los mas urgentes.
        List<ProductoViewDTO> productosStockBajo = productos.stream()
                .filter(ProductoViewDTO::isBajoStockMinimo)
                .sorted(Comparator.comparingInt(ProductoViewDTO::getStockActual))
                .limit(5)
                .collect(Collectors.toList());

        // Ultimas 5 ordenes creadas, sin importar el estado.
        List<OrdenCompraViewDTO> ordenesRecientes = ordenes.stream()
                .sorted(Comparator.comparing(OrdenCompraViewDTO::getFechaCreacion).reversed())
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("totalProveedoresActivos", totalProveedoresActivos);
        model.addAttribute("ordenesEnBorrador", ordenesEnBorrador);
        model.addAttribute("cantidadStockBajo", cantidadStockBajo);
        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("ordenesRecientes", ordenesRecientes);

        return "views/inicio";
    }
}
