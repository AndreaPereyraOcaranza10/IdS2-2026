package com.colegio.sistemaescolar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SEGURIDAD de la aplicación (Spring Security 6).
 *
 * <h2>Qué protege</h2>
 * <ol>
 *   <li><b>Autenticación</b> por formulario: usuario = email del docente, contraseña con hash BCrypt.
 *       Los usuarios se leen de la base con {@code ServicioDetallesUsuario}.</li>
 *   <li><b>Autorización por rol</b>:
 *       <ul>
 *         <li>Sitio público (inicio, grados, servicios, contacto, login, registro): libre.</li>
 *         <li>{@code /panel/docentes|grados|aulas|materias/**}: solo ADMIN.</li>
 *         <li>Resto de {@code /panel/**}: ADMIN o DOCENTE (dentro, cada servicio limita qué ve el docente).</li>
 *       </ul></li>
 *   <li><b>CSRF</b>: activo por defecto. Todo formulario POST lleva un token oculto (Thymeleaf lo agrega
 *       automáticamente cuando se usa {@code th:action}). Evita que otro sitio envíe formularios en nombre del usuario.</li>
 *   <li><b>Sesión</b>: se invalida al cerrar sesión y Spring cambia el ID de sesión al autenticar
 *       (protección contra fijación de sesión).</li>
 *   <li><b>Seguridad a nivel de método</b> ({@code @EnableMethodSecurity}): permite {@code @PreAuthorize}
 *       en los controladores como segunda barrera.</li>
 * </ol>
 *
 * <p>Se usa el estilo moderno con un bean {@code SecurityFilterChain} (el antiguo
 * {@code WebSecurityConfigurerAdapter} fue eliminado en Spring Security 6).</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    /**
     * Codificador de contraseñas. BCrypt aplica sal aleatoria y es deliberadamente lento,
     * lo que dificulta los ataques de fuerza bruta si se filtra la base de datos.
     */
    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
            // ---------- Reglas de acceso: se evalúan en orden, gana la primera que coincide ----------
            .authorizeHttpRequests(reglas -> reglas
                .requestMatchers("/", "/grados", "/servicios", "/contacto",
                        "/login", "/registro", "/error", "/error/**",
                        "/favicon.ico", "/recursos/**").permitAll()
                .requestMatchers("/panel/docentes/**", "/panel/grados/**",
                        "/panel/aulas/**", "/panel/materias/**").hasRole("ADMIN")
                // Alumnos: el DOCENTE solo consulta (lista/detalle); alta, edición y baja son del ADMIN.
                .requestMatchers("/panel/alumnos/nuevo", "/panel/alumnos/*/editar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/panel/alumnos/**").hasRole("ADMIN")
                .requestMatchers("/panel/**").hasAnyRole("ADMIN", "DOCENTE")
                .anyRequest().authenticated())

            // ---------- Inicio de sesión ----------
            .formLogin(formulario -> formulario
                .loginPage("/login")                 // página propia (plantilla Oinia)
                .loginProcessingUrl("/login")        // URL a la que envía el formulario (POST)
                .usernameParameter("email")          // el "usuario" es el correo
                .passwordParameter("password")
                .defaultSuccessUrl("/panel", true)   // siempre al panel luego de ingresar
                .failureUrl("/login?error")          // mensaje genérico (no revela si el correo existe)
                .permitAll())

            // ---------- Cierre de sesión (solo por POST, protegido por CSRF) ----------
            .logout(salida -> salida
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());

        return http.build();
    }
}
