package com.techstore.inventario.services;

import java.util.List;

/**
 * Contrato generico de operaciones CRUD que implementan todos los servicios
 * del sistema (Producto, Proveedor, Usuario, OrdenCompra).
 * @param <E> tipo de entidad sobre la que opera el servicio.
 */
public interface ServicioBase<E> {

    /** Devuelve todos los registros de la entidad. */
    List<E> findAll() throws Exception;

    /** Busca un registro por su id. Lanza Exception si no existe. */
    E findById(long id) throws Exception;

    /** Persiste una nueva entidad. */
    E saveOne(E entity) throws Exception;

    /** Actualiza una entidad existente identificada por id. */
    E updateOne(E entity, long id) throws Exception;

    /** Elimina un registro por id. Devuelve false si no existia. */
    boolean deleteById(long id) throws Exception;
}
