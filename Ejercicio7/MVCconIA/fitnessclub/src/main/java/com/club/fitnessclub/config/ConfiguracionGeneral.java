package com.club.fitnessclub.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de infraestructura compartidos.
 *
 * <p>Se expone un {@link Clock} como bean para que los servicios NO llamen a
 * LocalDateTime.now() directamente: en las pruebas unitarias se reemplaza por un
 * Clock.fixed(...) y los resultados (fechas de entrada, vencimientos, etc.) son
 * deterministas. En producción es el reloj del sistema con la zona horaria del servidor.
 */
@Configuration
public class ConfiguracionGeneral {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
