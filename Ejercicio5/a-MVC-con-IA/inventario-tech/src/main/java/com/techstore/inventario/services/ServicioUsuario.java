package com.techstore.inventario.services;

import com.techstore.inventario.entities.Usuario;
import com.techstore.inventario.repositories.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de gestion de Usuarios.
 * NOTA: la autenticacion (Spring Security / login) se pospuso para una etapa
 * posterior del proyecto. Los TODO marcados abajo indican donde se debe
 * incorporar el hasheo de contrasenias (PasswordEncoder) cuando se implemente.
 */
@Service
public class ServicioUsuario implements ServicioBase<Usuario> {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Override
    public List<Usuario> findAll() throws Exception {
        return repositorioUsuario.findAll();
    }

    @Override
    public Usuario findById(long id) throws Exception {
        return repositorioUsuario.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con id: " + id));
    }

    @Override
    public Usuario saveOne(Usuario entity) throws Exception {
        if (repositorioUsuario.existsByUsername(entity.getUsername())) {
            throw new Exception("Ya existe un usuario con ese username: " + entity.getUsername());
        }
        // TODO (seguridad pendiente): hashear entity.getPassword() con PasswordEncoder antes de guardar.
        return repositorioUsuario.save(entity);
    }

    @Override
    public Usuario updateOne(Usuario entity, long id) throws Exception {
        Usuario existente = findById(id);
        existente.setNombre(entity.getNombre());
        existente.setRol(entity.getRol());
        existente.setActivo(entity.isActivo());

        // Si no se envia password nueva, se conserva la actual (no se pisa con vacio/null).
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            // TODO (seguridad pendiente): hashear con PasswordEncoder.
            existente.setPassword(entity.getPassword());
        }
        return repositorioUsuario.save(existente);
    }

    @Override
    public boolean deleteById(long id) throws Exception {
        if (!repositorioUsuario.existsById(id)) {
            return false;
        }
        repositorioUsuario.deleteById(id);
        return true;
    }
}
