package com.colegio.sistemaescolar.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/**
 * Acceso cómodo al USUARIO AUTENTICADO desde cualquier capa (servicios, auditoría).
 *
 * <p>Spring Security guarda al usuario actual en el {@code SecurityContextHolder} (asociado al hilo
 * de la petición). Esta clase encapsula esa consulta para que el resto del código no dependa
 * de detalles de Spring Security.</p>
 */
@Component
public class ContextoUsuario {

    private static final String AUTORIDAD_ADMIN = "ROLE_ADMIN";

    /** Email (nombre de usuario) de quien está autenticado, o vacío si es anónimo / no hay sesión. */
    public Optional<String> email() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(autenticacion.getName());
    }

    /** true si el usuario actual tiene el rol ADMIN. */
    public boolean esAdmin() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            return false;
        }
        return autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> AUTORIDAD_ADMIN.equals(autoridad.getAuthority()));
    }

    /** true si el código se ejecuta dentro de una petición HTTP (y no en el arranque o un hilo interno). */
    public boolean haySolicitudWeb() {
        return RequestContextHolder.getRequestAttributes() != null;
    }
}
