package com.techstore.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada: una linea del formulario dinamico de orden de compra.
 * subtotal NO viaja desde el formulario: lo calcula el service (cantidad * precioUnitario)
 * para no confiar en un valor que el cliente podria manipular.
 */
public class DetalleOrdenCompraFormDTO {

    @NotNull(message = "Debe seleccionar un producto")
    private Long productoId;

    @NotNull(message = "Debe indicar la cantidad")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "Debe indicar el precio unitario")
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
