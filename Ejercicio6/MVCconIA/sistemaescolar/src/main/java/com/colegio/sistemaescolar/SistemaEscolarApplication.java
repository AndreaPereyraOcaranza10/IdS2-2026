package com.colegio.sistemaescolar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * PUNTO DE ENTRADA de la aplicación "Sistema de Gestión Escolar".
 *
 * <h2>Arquitectura general (MVC en capas)</h2>
 * <pre>
 *  Navegador
 *     |  HTTP
 *     v
 *  [Seguridad]  config/ConfiguracionSeguridad  (login, roles, CSRF)
 *     v
 *  [CONTROLLER] controllers/*   Recibe la petición, valida el DTO (@Valid) y elige la vista.
 *     v
 *  [SERVICE]    services/*      Reglas de negocio y transacciones (@Transactional).
 *     v            ^
 *  [MAPPER]     mappers/*       Convierte Entidad <-> DTO (las entidades NUNCA llegan a la vista).
 *     v
 *  [REPOSITORY] repositories/*  Spring Data JPA (ORM Hibernate) -> MySQL.
 *     v
 *  [ENTITY]     entities/*      Clases mapeadas a tablas; todas heredan de {@code Auditable}.
 *
 *  [VIEW] resources/templates/* Thymeleaf (HTML) con las plantillas Oinia (sitio público) y
 *                               SB Admin (panel de gestión), ambas basadas en Bootstrap.
 * </pre>
 *
 * <h2>Anotaciones de esta clase</h2>
 * <ul>
 *   <li>{@code @SpringBootApplication}: equivale a {@code @Configuration} + {@code @EnableAutoConfiguration}
 *       + {@code @ComponentScan}. Escanea este paquete y sus subpaquetes buscando beans
 *       ({@code @Component}, {@code @Service}, {@code @Controller}, {@code @Repository}...).</li>
 *   <li>{@code @ConfigurationPropertiesScan}: registra automáticamente las clases
 *       {@code @ConfigurationProperties} (ver {@code PropiedadesColegio}).</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SistemaEscolarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaEscolarApplication.class, args);
    }
}
