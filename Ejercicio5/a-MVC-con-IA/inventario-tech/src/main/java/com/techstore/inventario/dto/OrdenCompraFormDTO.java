package com.techstore.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO de entrada: datos que llegan desde el formulario para crear una
 * orden de compra en estado BORRADOR. No incluye numeroOrden, estado ni
 * total: esos campos los calcula/asigna el service, nunca el usuario.
 */
public class OrdenCompraFormDTO {

    @NotNull(message = "Debe seleccionar un proveedor")
    private Long proveedorId;

    /** Id del usuario que genera la orden (por ahora, sin login, se elige de un combo). */
    @NotNull(message = "Debe indicar el usuario")
    private Long usuarioId;

    @NotEmpty(message = "La orden debe tener al menos una linea")
    @Valid
    private List<DetalleOrdenCompraFormDTO> detalles;

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<DetalleOrdenCompraFormDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOrdenCompraFormDTO> detalles) { this.detalles = detalles; }
}
