package com.techstore.inventario.mapper;

import com.techstore.inventario.dto.ProductoFormDTO;
import com.techstore.inventario.dto.ProductoViewDTO;
import com.techstore.inventario.entities.Producto;

/** Traduce entre la entidad Producto y sus DTOs de entrada/salida. */
public class ProductoMapper {

    /** Arma una entidad Producto nueva a partir del formulario (para alta). */
    public static Producto aEntidadNueva(ProductoFormDTO form) {
        Producto producto = new Producto();
        producto.setCodigo(form.getCodigo());
        producto.setNombre(form.getNombre());
        producto.setDescripcion(form.getDescripcion());
        producto.setCategoria(form.getCategoria());
        producto.setPrecioCompra(form.getPrecioCompra());
        producto.setPrecioVenta(form.getPrecioVenta());
        producto.setStockMinimo(form.getStockMinimo());
        producto.setActivo(form.isActivo());
        producto.setStockActual(0); // todo producto nuevo arranca en 0; el stock entra por OrdenCompra
        return producto;
    }

    /**
     * Arma una entidad Producto "de paso" con los datos del formulario de edicion.
     * Se usa como parametro de ServicioProducto.updateOne(), que copia solo los
     * campos editables sobre la entidad real ya persistida.
     */
    public static Producto aEntidadParaUpdate(ProductoFormDTO form) {
        Producto producto = new Producto();
        producto.setNombre(form.getNombre());
        producto.setDescripcion(form.getDescripcion());
        producto.setCategoria(form.getCategoria());
        producto.setPrecioCompra(form.getPrecioCompra());
        producto.setPrecioVenta(form.getPrecioVenta());
        producto.setStockMinimo(form.getStockMinimo());
        producto.setActivo(form.isActivo());
        return producto;
    }

    /** Convierte la entidad persistida a su DTO de salida para la vista. */
    public static ProductoViewDTO aViewDTO(Producto producto) {
        ProductoViewDTO dto = new ProductoViewDTO();
        dto.setId(producto.getId());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCategoria(producto.getCategoria());
        dto.setPrecioCompra(producto.getPrecioCompra());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setStockActual(producto.getStockActual());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setActivo(producto.isActivo());
        dto.setBajoStockMinimo(producto.getStockActual() <= producto.getStockMinimo());
        return dto;
    }

    /** Para precargar el formulario de edicion con los datos actuales del producto. */
    public static ProductoFormDTO aFormDTO(Producto producto) {
        ProductoFormDTO form = new ProductoFormDTO();
        form.setId(producto.getId());
        form.setCodigo(producto.getCodigo());
        form.setNombre(producto.getNombre());
        form.setDescripcion(producto.getDescripcion());
        form.setCategoria(producto.getCategoria());
        form.setPrecioCompra(producto.getPrecioCompra());
        form.setPrecioVenta(producto.getPrecioVenta());
        form.setStockMinimo(producto.getStockMinimo());
        form.setActivo(producto.isActivo());
        return form;
    }
}
