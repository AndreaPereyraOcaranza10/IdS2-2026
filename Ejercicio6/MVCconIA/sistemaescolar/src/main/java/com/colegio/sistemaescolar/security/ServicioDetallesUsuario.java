package com.colegio.sistemaescolar.security;

import com.colegio.sistemaescolar.entities.Usuario;
import com.colegio.sistemaescolar.repositories.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puente entre nuestra tabla {@code usuarios} y Spring Security.
 *
 * <p>Cuando alguien envía el formulario de login, Spring Security llama a
 * {@link #loadUserByUsername(String)} con el valor del campo "email", obtiene el hash BCrypt
 * guardado y lo compara con la contraseña ingresada (usando el {@code PasswordEncoder} configurado).
 * Al existir un único bean {@code UserDetailsService}, Spring Security lo usa automáticamente
 * (no hace falta declarar el AuthenticationProvider a mano).</p>
 */
@Service
@RequiredArgsConstructor
public class ServicioDetallesUsuario implements UserDetailsService {

    private final RepositorioUsuario repositorioUsuario;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String correo = email == null ? "" : email.trim();
        Usuario usuario = repositorioUsuario.findByEmailIgnoreCase(correo)
                .filter(u -> !u.isEliminado())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPasswordHash())
                // roles("DOCENTE") crea la autoridad "ROLE_DOCENTE"
                .roles(usuario.getRol().name())
                // una cuenta deshabilitada no puede iniciar sesión
                .disabled(!usuario.isHabilitado())
                .build();
    }
}
