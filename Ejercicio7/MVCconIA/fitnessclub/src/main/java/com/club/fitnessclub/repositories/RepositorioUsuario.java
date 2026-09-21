package com.club.fitnessclub.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.club.fitnessclub.entities.Usuario;
import com.club.fitnessclub.enums.Rol;

/** Repositorio de Usuario (autenticación y ABM de usuarios del sistema). */
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    /** Usado por Spring Security al autenticar: solo usuarios vigentes. */
    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    List<Usuario> findByActivoTrueOrderByUsernameAsc();

    /** ¿Existe el username? (sin filtrar por activo: la restricción única de la BD incluye bajas). */
    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, long id);

    /** Cantidad de usuarios vigentes con un rol (protege al último ADMIN). */
    long countByRolAndActivoTrue(Rol rol);
}
