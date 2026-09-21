package com.club.fitnessclub.services;

import java.util.List;

/**
 * Contrato CRUD genérico de la capa de SERVICIO.
 *
 * @param <D> tipo de DTO con el que trabaja el servicio.
 *
 * <p>DESVÍO respecto de RedSelection (ServicioBase&lt;E&gt;): aquí el parámetro es el DTO, no la
 * entidad. Así ninguna entidad JPA sale de la capa de servicio:
 * <pre>
 *   Controlador  &lt;-- DTO --&gt;  Servicio  &lt;-- Entidad --&gt;  Repositorio
 * </pre>
 * La conversión DTO &lt;-&gt; Entidad la hacen los Mapeadores dentro del servicio.
 *
 * <p>Todos los métodos declaran "throws Exception" (convención del proyecto).
 */
public interface ServicioBase<D> {

    /** Devuelve todos los registros vigentes (activo = true). */
    List<D> findAll() throws Exception;

    /** Busca un registro vigente por id. Lanza ExcepcionNegocio si no existe. */
    D findById(long id) throws Exception;

    /** Da de alta un registro nuevo y devuelve el DTO con el id generado. */
    D save(D dto) throws Exception;

    /** Modifica un registro existente. */
    D update(long id, D dto) throws Exception;

    /** BAJA LÓGICA: marca el registro como inactivo (no borra la fila). */
    void delete(long id) throws Exception;
}
