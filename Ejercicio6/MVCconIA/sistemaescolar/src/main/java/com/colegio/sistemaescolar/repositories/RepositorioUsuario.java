package com.colegio.sistemaescolar.repositories;

import com.colegio.sistemaescolar.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** REPOSITORIO de {@link Usuario}. La búsqueda por correo ignora mayúsculas/minúsculas. */
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
