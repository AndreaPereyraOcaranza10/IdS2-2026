package com.techstore.inventario.dto;

import java.math.BigDecimal;

/**
 * DTO de salida: lo que se muestra en listado/detalle de Producto.
 * A diferencia del FormDTO, si incluye stockActual (de solo lectura en la vista)
 * y un flag bajoStockMinimo, util para resaltar en la lista que productos hay que reponer.
 */
public class ProductoViewDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private Integer stockMinimo;
    private boolean activo;
    private boolean bajoStockMinimo;

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

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isBajoStockMinimo() { return bajoStockMinimo; }
    public void setBajoStockMinimo(boolean bajoStockMinimo) { this.bajoStockMinimo = bajoStockMinimo; }
}
