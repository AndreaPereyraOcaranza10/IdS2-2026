package com.techstore.inventario.repositories;

import com.techstore.inventario.entities.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de OrdenCompra. */
@Repository
public interface RepositorioOrdenCompra extends JpaRepository<OrdenCompra, Long> {
    /** Cuenta cuantas ordenes existen con un prefijo dado (usado para numerar OC-fecha-NN). */
    long countByNumeroOrdenStartingWith(String prefijo);
}
