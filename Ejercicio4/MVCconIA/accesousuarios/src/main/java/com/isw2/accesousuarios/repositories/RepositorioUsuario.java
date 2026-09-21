package com.isw2.accesousuarios.repositories;

import com.isw2.accesousuarios.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CAPA: REPOSITORIO (acceso a datos)
 * ============================================================================
 * Acceso a la tabla "usuarios". Ver {@link RepositorioPersona} para el detalle
 * de @Repository y JpaRepository.
 */
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    /**
     * Busca el usuario cuya Persona tiene el correo indicado.
     *
     * Query method con navegación de propiedades: "PersonaCorreo" se interpreta
     * como usuario.persona.correo, y Spring Data genera el JOIN entre usuarios y
     * personas. Devuelve Optional para representar "no existe" sin usar null:
     * es exactamente el caso "usuario no registrado" del enunciado.
     */
    Optional<Usuario> findByPersonaCorreo(String correo);
}
