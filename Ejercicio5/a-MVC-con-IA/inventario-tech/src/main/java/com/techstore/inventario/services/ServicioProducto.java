package com.techstore.inventario.services;

import com.techstore.inventario.entities.Producto;
import com.techstore.inventario.repositories.RepositorioProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de gestion de Productos.
 * IMPORTANTE: este servicio NO modifica el campo stockActual en updateOne().
 * El stock solo se actualiza a traves de ServicioOrdenCompra (confirmar/anular),
 * para que el unico camino de entrada de stock sea una orden de compra registrada
 * y asi mantener trazabilidad de todo movimiento de inventario.
 */
@Service
public class ServicioProducto implements ServicioBase<Producto> {

    @Autowired
    private RepositorioProducto repositorioProducto;

    @Override
    public List<Producto> findAll() throws Exception {
        return repositorioProducto.findAll();
    }

    @Override
    public Producto findById(long id) throws Exception {
        return repositorioProducto.findById(id)
                .orElseThrow(() -> new Exception("Producto no encontrado con id: " + id));
    }

    @Override
    public Producto saveOne(Producto entity) throws Exception {
        return repositorioProducto.save(entity);
    }

    @Override
    public Producto updateOne(Producto entity, long id) throws Exception {
        Producto existente = findById(id);

        // Se copian solo los campos editables por el usuario.
        // stockActual queda deliberadamente afuera: ver comentario de clase.
        existente.setNombre(entity.getNombre());
        existente.setDescripcion(entity.getDescripcion());
        existente.setCategoria(entity.getCategoria());
        existente.setPrecioCompra(entity.getPrecioCompra());
        existente.setPrecioVenta(entity.getPrecioVenta());
        existente.setStockMinimo(entity.getStockMinimo());
        existente.setActivo(entity.isActivo());

        return repositorioProducto.save(existente);
    }

    @Override
    public boolean deleteById(long id) throws Exception {
        if (!repositorioProducto.existsById(id)) {
            return false;
        }
        repositorioProducto.deleteById(id);
        return true;
    }
}
