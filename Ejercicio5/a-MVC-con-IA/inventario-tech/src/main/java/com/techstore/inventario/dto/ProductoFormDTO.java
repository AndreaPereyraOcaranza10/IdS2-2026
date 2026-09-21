package com.techstore.inventario.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada: datos del formulario de alta/edicion de Producto.
 * No incluye stockActual: ese campo nunca se edita manualmente desde aca
 * (ver comentario en ServicioProducto), solo se ve en el DTO de salida.
 */
public class ProductoFormDTO {

    private Long id; // null en alta, con valor en edicion

    @NotBlank(message = "El codigo (SKU) es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private String categoria;

    @NotNull(message = "El precio de compra es obligatorio")
    @Positive(message = "El precio de compra debe ser mayor a 0")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock minimo es obligatorio")
    @PositiveOrZero(message = "El stock minimo no puede ser negativo")
    private Integer stockMinimo;

    private boolean activo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
