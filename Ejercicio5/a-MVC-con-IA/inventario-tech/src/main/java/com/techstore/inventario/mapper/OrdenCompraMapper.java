package com.techstore.inventario.mapper;

import com.techstore.inventario.dto.*;
import com.techstore.inventario.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduce entre las entidades de OrdenCompra/DetalleOrdenCompra y sus DTOs
 * correspondientes. No depende de ningun repositorio: recibe las entidades
 * Proveedor/Usuario/Producto ya resueltas por el Controller/Service.
 */
public class OrdenCompraMapper {

    /**
     * Arma las entidades DetalleOrdenCompra a partir del formulario, resolviendo
     * cada Producto mediante la funcion lookup provista (normalmente ServicioProducto::findById).
     * El subtotal se deja en null: lo termina de calcular ServicioOrdenCompra.crearBorrador().
     */
    public static List<DetalleOrdenCompra> aEntidadesDetalle(
            List<DetalleOrdenCompraFormDTO> detallesForm,
            java.util.function.Function<Long, Producto> lookupProducto) throws Exception {

        List<DetalleOrdenCompra> resultado = new ArrayList<>();
        for (DetalleOrdenCompraFormDTO df : detallesForm) {
            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setProducto(lookupProducto.apply(df.getProductoId()));
            detalle.setCantidad(df.getCantidad());
            detalle.setPrecioUnitario(df.getPrecioUnitario());
            resultado.add(detalle);
        }
        return resultado;
    }

    /** Convierte la entidad OrdenCompra (ya persistida) a su DTO de salida para la vista. */
    public static OrdenCompraViewDTO aViewDTO(OrdenCompra orden) {
        OrdenCompraViewDTO dto = new OrdenCompraViewDTO();
        dto.setId(orden.getId());
        dto.setNumeroOrden(orden.getNumeroOrden());
        dto.setProveedorRazonSocial(orden.getProveedor().getRazonSocial());
        dto.setUsuarioNombre(orden.getUsuario().getNombre());
        dto.setFechaCreacion(orden.getFechaCreacion());
        dto.setFechaConfirmacion(orden.getFechaConfirmacion());
        dto.setFechaAnulacion(orden.getFechaAnulacion());
        dto.setEstado(orden.getEstado().name());
        dto.setTotal(orden.getTotal());
        dto.setDetalles(
                orden.getDetalles().stream()
                        .map(OrdenCompraMapper::aDetalleViewDTO)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    private static DetalleOrdenCompraViewDTO aDetalleViewDTO(DetalleOrdenCompra detalle) {
        DetalleOrdenCompraViewDTO dto = new DetalleOrdenCompraViewDTO();
        dto.setId(detalle.getId());
        dto.setProductoCodigo(detalle.getProducto().getCodigo());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
