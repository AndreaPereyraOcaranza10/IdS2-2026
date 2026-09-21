package com.techstore.inventario.entities;

/**
 * Estados posibles de una OrdenCompra.
 * BORRADOR: recien creada, editable, no impacto stock.
 * CONFIRMADA: impacto stock (sumo cantidades a los productos).
 * ANULADA: cancelada; si venia de CONFIRMADA, se revirtio el stock sumado.
 */
public enum EstadoOrden {
    BORRADOR,
    CONFIRMADA,
    ANULADA
}
