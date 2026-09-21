package com.techstore.inventario.mapper;

import com.techstore.inventario.dto.UsuarioFormDTO;
import com.techstore.inventario.dto.UsuarioViewDTO;
import com.techstore.inventario.entities.Usuario;

/** Traduce entre la entidad Usuario y sus DTOs de entrada/salida. */
public class UsuarioMapper {

    public static Usuario aEntidadNueva(UsuarioFormDTO form) {
        Usuario usuario = new Usuario();
        usuario.setUsername(form.getUsername());
        usuario.setPassword(form.getPassword());
        usuario.setNombre(form.getNombre());
        usuario.setRol(form.getRol());
        usuario.setActivo(form.isActivo());
        return usuario;
    }

    /** Para update: incluye password solo si el form la trae (se maneja en ServicioUsuario.updateOne). */
    public static Usuario aEntidadParaUpdate(UsuarioFormDTO form) {
        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre());
        usuario.setRol(form.getRol());
        usuario.setActivo(form.isActivo());
        usuario.setPassword(form.getPassword()); // puede venir null/blank; ServicioUsuario lo respeta
        return usuario;
    }

    public static UsuarioViewDTO aViewDTO(Usuario usuario) {
        UsuarioViewDTO dto = new UsuarioViewDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setNombre(usuario.getNombre());
        dto.setRol(usuario.getRol().name());
        dto.setActivo(usuario.isActivo());
        return dto;
    }

    /** Para precargar el formulario de edicion. La password NO se precarga (queda vacia). */
    public static UsuarioFormDTO aFormDTO(Usuario usuario) {
        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setId(usuario.getId());
        form.setUsername(usuario.getUsername());
        form.setNombre(usuario.getNombre());
        form.setRol(usuario.getRol());
        form.setActivo(usuario.isActivo());
        // password se deja vacia a proposito: no se muestra la actual por seguridad,
        // y si el usuario no toca el campo, ServicioUsuario.updateOne() la conserva.
        return form;
    }
}
