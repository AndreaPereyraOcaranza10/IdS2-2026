package com.club.fitnessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de las páginas PÚBLICAS y de acceso (layout público con la plantilla Health &amp; Fitness).
 *
 * <p>@Controller: clase de la capa web. Sus métodos devuelven el NOMBRE de una vista Thymeleaf
 * (archivo en src/main/resources/templates, sin extensión). @GetMapping mapea una URL con
 * método HTTP GET.
 *
 * <p>El POST de /login NO está acá: lo procesa Spring Security (ver ConfiguracionSeguridad).
 */
@Controller
public class ControladorInicio {

    /** Página de inicio pública del club. */
    @GetMapping("/")
    public String inicio() {
        return "publico/index";
    }

    /** Formulario de inicio de sesión. Recibe ?error y ?logout para mostrar mensajes. */
    @GetMapping("/login")
    public String login() {
        return "publico/login";
    }

    /** Página mostrada cuando un usuario autenticado intenta entrar a una sección sin permiso (HTTP 403). */
    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
