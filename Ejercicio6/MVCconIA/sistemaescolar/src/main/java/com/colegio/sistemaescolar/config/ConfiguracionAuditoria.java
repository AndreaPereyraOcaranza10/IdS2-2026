package com.colegio.sistemaescolar.config;

import com.colegio.sistemaescolar.security.ContextoUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AUDITORÍA DE ENTIDADES: configuración de Spring Data JPA Auditing.
 *
 * <ul>
 *   <li>{@code @EnableJpaAuditing}: activa el listener que completa los campos
 *       {@code @CreatedDate/@CreatedBy/@LastModifiedDate/@LastModifiedBy} de {@code Auditable}.</li>
 *   <li>{@code auditorAwareRef}: bean que responde "¿quién está haciendo el cambio?".</li>
 *   <li>{@code dateTimeProviderRef}: bean que responde "¿qué hora es?" (usa el {@code Clock}).</li>
 * </ul>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorActual", dateTimeProviderRef = "proveedorFechaHora")
public class ConfiguracionAuditoria {

    /** Auditor = email del usuario autenticado; "sistema" en tareas internas; "registro-publico" si es anónimo. */
    @Bean
    public AuditorAware<String> auditorActual(ContextoUsuario contexto) {
        return () -> {
            Optional<String> email = contexto.email();
            if (email.isPresent()) {
                return email;
            }
            return Optional.of(contexto.haySolicitudWeb() ? "registro-publico" : "sistema");
        };
    }

    @Bean
    public DateTimeProvider proveedorFechaHora(Clock reloj) {
        return () -> Optional.of(LocalDateTime.now(reloj));
    }
}
