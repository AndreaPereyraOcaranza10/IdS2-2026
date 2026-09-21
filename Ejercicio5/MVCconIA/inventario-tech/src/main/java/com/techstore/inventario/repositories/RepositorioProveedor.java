package com.techstore.inventario.repositories;

import com.techstore.inventario.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Acceso a datos de Proveedor. cuit actua como identificador de negocio. */
@Repository
public interface RepositorioProveedor extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByCuit(String cuit);
    boolean existsByCuit(String cuit);
}
