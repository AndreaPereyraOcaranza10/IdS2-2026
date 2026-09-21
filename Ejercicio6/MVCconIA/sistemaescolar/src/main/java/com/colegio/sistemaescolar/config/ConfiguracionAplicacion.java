package com.colegio.sistemaescolar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

/**
 * Configuración general de la aplicación.
 *
 * <ul>
 *   <li>{@code @Configuration}: clase que declara beans con métodos {@code @Bean}.</li>
 *   <li>{@code @EnableAsync}: habilita {@code @Async}. El envío de correos se ejecuta en otro hilo
 *       para no demorar (ni hacer fallar) el registro del docente si el servidor SMTP tarda o cae.</li>
 *   <li>{@code Clock}: se inyecta en servicios y en la auditoría en vez de llamar a
 *       {@code LocalDate.now()} directamente. Ventaja: las pruebas pueden fijar la fecha.</li>
 * </ul>
 */
@Configuration
@EnableAsync
public class ConfiguracionAplicacion {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
