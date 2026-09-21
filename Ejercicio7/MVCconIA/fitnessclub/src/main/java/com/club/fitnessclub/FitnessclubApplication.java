package com.club.fitnessclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * <ul>
 *   <li>@ComponentScan escanea este paquete y sus subpaquetes (config, controllers, dtos,
 *       entities, mappers, repositories, services, security), por eso esta clase debe
 *       quedar en la raíz del paquete base.</li>
 *   <li>@EnableAutoConfiguration configura Tomcat, JPA/Hibernate, Thymeleaf y Spring
 *       Security a partir de las dependencias del pom.xml y de application.properties.</li>
 * </ul>
 *
 * <p>ARQUITECTURA EN CAPAS (MVC + Servicio + Persistencia):
 * <pre>
 *   Navegador
 *      |  HTTP (formularios / URLs)
 *   [VISTA]        templates/*.html (Thymeleaf)           -> solo ve DTOs
 *   [CONTROLADOR]  controllers/Controlador*.java          -> @Controller, recibe y valida DTOs
 *   [SERVICIO]     services/Servicio*.java                -> @Service, reglas de negocio, @Transactional
 *   [MAPEADOR]     mappers/Mapeador*.java                 -> Entidad <-> DTO
 *   [REPOSITORIO]  repositories/Repositorio*.java         -> Spring Data JPA (ORM)
 *   [MODELO]       entities/*.java                        -> @Entity (Hibernate) -> tablas MySQL
 * </pre>
 * Regla de oro: las ENTIDADES JPA nunca llegan al controlador ni a la vista; entre
 * capas superiores viajan únicamente DTOs (paquete dtos).
 */
@SpringBootApplication
public class FitnessclubApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessclubApplication.class, args);
    }
}
