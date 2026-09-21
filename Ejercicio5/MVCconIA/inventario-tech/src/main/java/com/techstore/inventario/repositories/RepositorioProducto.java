package com.techstore.inventario.repositories;

import com.techstore.inventario.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Acceso a datos de Producto. codigo actua como identificador de negocio (SKU). */
@Repository
public interface RepositorioProducto extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
