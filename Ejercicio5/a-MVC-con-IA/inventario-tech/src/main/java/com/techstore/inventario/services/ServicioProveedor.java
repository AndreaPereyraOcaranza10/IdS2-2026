package com.techstore.inventario.services;

import com.techstore.inventario.entities.Proveedor;
import com.techstore.inventario.repositories.RepositorioProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** Servicio de gestion de Proveedores. */
@Service
public class ServicioProveedor implements ServicioBase<Proveedor> {

    @Autowired
    private RepositorioProveedor repositorioProveedor;

    @Override
    public List<Proveedor> findAll() throws Exception {
        return repositorioProveedor.findAll();
    }

    @Override
    public Proveedor findById(long id) throws Exception {
        return repositorioProveedor.findById(id)
                .orElseThrow(() -> new Exception("Proveedor no encontrado con id: " + id));
    }

    @Override
    public Proveedor saveOne(Proveedor entity) throws Exception {
        // El CUIT es la clave de negocio: no puede haber dos proveedores con el mismo.
        if (repositorioProveedor.existsByCuit(entity.getCuit())) {
            throw new Exception("Ya existe un proveedor con el CUIT: " + entity.getCuit());
        }
        return repositorioProveedor.save(entity);
    }

    @Override
    public Proveedor updateOne(Proveedor entity, long id) throws Exception {
        Proveedor existente = findById(id);
        existente.setRazonSocial(entity.getRazonSocial());
        existente.setTelefono(entity.getTelefono());
        existente.setEmail(entity.getEmail());
        existente.setDireccion(entity.getDireccion());
        existente.setActivo(entity.isActivo());
        // El CUIT no se edita una vez creado: las ordenes de compra ya emitidas
        // quedan asociadas al proveedor por id, pero cambiar el CUIT rompería
        // la trazabilidad fiscal del historial de compras.
        return repositorioProveedor.save(existente);
    }

    @Override
    public boolean deleteById(long id) throws Exception {
        if (!repositorioProveedor.existsById(id)) {
            return false;
        }
        repositorioProveedor.deleteById(id);
        return true;
    }
}
