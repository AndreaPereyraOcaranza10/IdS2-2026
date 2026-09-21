package com.techstore.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida: lo que se muestra en las vistas (lista, detalle) de una
 * orden de compra ya persistida. Aplana los datos de Proveedor/Usuario
 * a lo estrictamente necesario para no acoplar la vista a esas entidades.
 */
public class OrdenCompraViewDTO {

    private Long id;
    private String numeroOrden;
    private String proveedorRazonSocial;
    private String usuarioNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaAnulacion;
    private String estado;
    private BigDecimal total;
    private List<DetalleOrdenCompraViewDTO> detalles;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public String getProveedorRazonSocial() { return proveedorRazonSocial; }
    public void setProveedorRazonSocial(String proveedorRazonSocial) { this.proveedorRazonSocial = proveedorRazonSocial; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }

    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<DetalleOrdenCompraViewDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOrdenCompraViewDTO> detalles) { this.detalles = detalles; }
}
