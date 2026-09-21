package com.colegio.sistemaescolar.services;

import java.util.List;

/**
 * Contrato genérico de un servicio CRUD (Alta, Baja, Modificación y consulta).
 *
 * <p>Es genérico sobre el DTO ({@code D}), NO sobre la entidad: hacia afuera del servicio solo salen
 * y entran DTO, de modo que las entidades JPA nunca cruzan hacia el controlador.</p>
 *
 * <p>Los métodos declaran {@code throws Exception} (convención del proyecto): las implementaciones
 * concretas pueden acotarlo a {@code ExcepcionNegocio}.</p>
 *
 * @param <D> tipo del DTO que maneja el servicio
 */
public interface ServicioBase<D> {

    /** Registros vigentes (no dados de baja). */
    List<D> listar() throws Exception;

    D buscarPorId(long id) throws Exception;

    D crear(D dto) throws Exception;

    D actualizar(long id, D dto) throws Exception;

    /** Baja lógica. */
    void eliminar(long id) throws Exception;
}
