package com.club.fitnessclub.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuración de la AUDITORÍA DE ENTIDADES.
 *
 * <p>@EnableJpaAuditing activa el listener AuditingEntityListener (declarado en la
 * clase base {@code Auditable}). Ese listener completa automáticamente, al hacer
 * persist/update:
 * <ul>
 *   <li>@CreatedDate / @LastModifiedDate: fecha y hora (las aporta Spring).</li>
 *   <li>@CreatedBy / @LastModifiedBy: QUIÉN lo hizo (lo aporta el bean AuditorAware).</li>
 * </ul>
 * El "auditor" es el username del usuario autenticado en Spring Security. Si no
 * hay nadie logueado (carga inicial de datos, tareas internas, tests) se usa "SISTEMA".
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class ConfiguracionAuditoria {

    /** Nombre usado como auditor cuando no hay un usuario autenticado. */
    private static final String AUDITOR_SISTEMA = "SISTEMA";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Sin autenticación, o sesión anónima: lo registra el sistema.
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return Optional.of(AUDITOR_SISTEMA);
            }
            return Optional.of(auth.getName());
        };
    }
}
