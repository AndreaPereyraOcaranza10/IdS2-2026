package com.club.fitnessclub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SEGURIDAD de la aplicación (Spring Security 6).
 *
 * <p>Usa el enfoque moderno de un bean SecurityFilterChain (WebSecurityConfigurerAdapter está
 * deprecado y fue eliminado).
 *
 * <p>AUTENTICACIÓN: formulario de login propio (/login, vista del layout público) que valida contra
 * la tabla usuario mediante {@code ServicioUserDetails}; las contraseñas se comparan con BCrypt.
 *
 * <p>AUTORIZACIÓN por rol (la primera regla que coincide es la que se aplica):
 * <pre>
 *   Público           : /, /login, /recursos/**, /error
 *   ADMIN + RECEPCION : /panel, /accesos/**, GET /personas (listado) y GET /personas/{id}/rostro (foto)
 *   Solo ADMIN        : /familias/**, /personas/** (alta, edición, baja), /cuotas/**, /pagos/**, /usuarios/**
 * </pre>
 * Además, las vistas ocultan los botones según el rol (sec:authorize de thymeleaf-extras-springsecurity6),
 * pero la protección REAL está acá: nunca se confía solo en ocultar botones.
 *
 * <p>CSRF: queda habilitado (por defecto). Los formularios Thymeleaf con th:action incluyen el
 * token automáticamente; por eso el logout es un POST (no un enlace).
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    /** BCrypt: hash con sal y costo configurable; es el estándar para guardar contraseñas. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // --- Zona pública ---
                .requestMatchers("/", "/login", "/error", "/favicon.ico", "/recursos/**").permitAll()
                // --- Recepción y Admin ---
                .requestMatchers(HttpMethod.GET, "/personas", "/personas/*/rostro").hasAnyRole("ADMIN", "RECEPCION")
                .requestMatchers("/accesos/**").hasAnyRole("ADMIN", "RECEPCION")
                // --- Solo Admin ---
                .requestMatchers("/familias/**", "/personas/**", "/cuotas/**", "/pagos/**", "/usuarios/**")
                    .hasRole("ADMIN")
                // --- Cualquier usuario autenticado ---
                .requestMatchers("/panel", "/acceso-denegado").authenticated()
                // Todo lo demás requiere estar autenticado (política por defecto: denegar).
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")              // GET /login: nuestra vista (ControladorInicio)
                .loginProcessingUrl("/login")     // POST /login: lo procesa Spring Security
                .defaultSuccessUrl("/panel", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")             // POST /logout (con token CSRF)
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));

        return http.build();
    }
}
