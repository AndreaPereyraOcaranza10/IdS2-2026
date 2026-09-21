package com.club.fitnessclub.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.club.fitnessclub.entities.Usuario;
import com.club.fitnessclub.repositories.RepositorioUsuario;

import lombok.RequiredArgsConstructor;

/**
 * Puente entre Spring Security y nuestra base de datos: cuando alguien intenta iniciar sesión,
 * Spring Security invoca loadUserByUsername() para obtener el usuario, compara la contraseña
 * ingresada contra el hash BCrypt guardado y, si coincide, crea la sesión con los roles.
 *
 * <p>Spring Boot detecta automáticamente este bean (único UserDetailsService) y el bean
 * PasswordEncoder de ConfiguracionSeguridad, y arma el AuthenticationManager con ambos.
 * Solo autentican los usuarios vigentes (activo = true).
 */
@Service
@RequiredArgsConstructor
public class ServicioUserDetails implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // roles("ADMIN") => autoridad "ROLE_ADMIN" (el prefijo ROLE_ lo agrega Spring Security).
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
