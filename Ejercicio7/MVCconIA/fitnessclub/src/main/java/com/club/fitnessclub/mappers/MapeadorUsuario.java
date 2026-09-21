package com.club.fitnessclub.mappers;

import org.springframework.stereotype.Component;

import com.club.fitnessclub.dtos.UsuarioDTO;
import com.club.fitnessclub.entities.Usuario;

/**
 * Mapeador Usuario (entidad) <-> UsuarioDTO.
 *
 * <p>SEGURIDAD: el hash de la contraseña NUNCA se copia al DTO, y la contraseña del DTO NO se
 * copia a la entidad aquí: el servicio la hashea con BCrypt antes de asignarla.
 */
@Component
public class MapeadorUsuario {

    public UsuarioDTO aDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.isActivo());
        return dto;
    }

    public Usuario aEntidad(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername().trim());
        usuario.setRol(dto.getRol());
        return usuario;
    }

    public void actualizarEntidad(Usuario usuario, UsuarioDTO dto) {
        usuario.setUsername(dto.getUsername().trim());
        usuario.setRol(dto.getRol());
    }
}
