package com.techstore.inventario.mapper;

import com.techstore.inventario.dto.ProveedorFormDTO;
import com.techstore.inventario.dto.ProveedorViewDTO;
import com.techstore.inventario.entities.Proveedor;

/** Traduce entre la entidad Proveedor y sus DTOs de entrada/salida. */
public class ProveedorMapper {

    public static Proveedor aEntidadNueva(ProveedorFormDTO form) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(form.getRazonSocial());
        proveedor.setCuit(form.getCuit());
        proveedor.setTelefono(form.getTelefono());
        proveedor.setEmail(form.getEmail());
        proveedor.setDireccion(form.getDireccion());
        proveedor.setActivo(form.isActivo());
        return proveedor;
    }

    /** Para update: no incluye cuit, que es inmutable una vez creado (ver ServicioProveedor). */
    public static Proveedor aEntidadParaUpdate(ProveedorFormDTO form) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(form.getRazonSocial());
        proveedor.setTelefono(form.getTelefono());
        proveedor.setEmail(form.getEmail());
        proveedor.setDireccion(form.getDireccion());
        proveedor.setActivo(form.isActivo());
        return proveedor;
    }

    public static ProveedorViewDTO aViewDTO(Proveedor proveedor) {
        ProveedorViewDTO dto = new ProveedorViewDTO();
        dto.setId(proveedor.getId());
        dto.setRazonSocial(proveedor.getRazonSocial());
        dto.setCuit(proveedor.getCuit());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        dto.setActivo(proveedor.isActivo());
        return dto;
    }

    public static ProveedorFormDTO aFormDTO(Proveedor proveedor) {
        ProveedorFormDTO form = new ProveedorFormDTO();
        form.setId(proveedor.getId());
        form.setRazonSocial(proveedor.getRazonSocial());
        form.setCuit(proveedor.getCuit());
        form.setTelefono(proveedor.getTelefono());
        form.setEmail(proveedor.getEmail());
        form.setDireccion(proveedor.getDireccion());
        form.setActivo(proveedor.isActivo());
        return form;
    }
}
