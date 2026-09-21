package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.GradoDTO;
import com.colegio.sistemaescolar.services.ServicioGrado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * CONTROLADOR del sitio público (no requiere iniciar sesión). Vistas basadas en la plantilla Oinia (Bootstrap 4).
 *
 * <h2>Capa Controller (MVC)</h2>
 * Recibe la petición HTTP, pide datos al servicio, los coloca en el {@link Model} y devuelve el NOMBRE de la plantilla
 * Thymeleaf (la vista). No contiene lógica de negocio ni toca repositorios.
 *
 * <h2>Anotaciones</h2>
 * <ul>
 *   <li>{@code @Controller}: devuelve nombres de vistas HTML (a diferencia de {@code @RestController}, que devuelve JSON).</li>
 *   <li>{@code @GetMapping}: atiende peticiones HTTP GET de esa URL.</li>
 *   <li>{@code @RequiredArgsConstructor} (Lombok): genera el constructor con los campos {@code final} = inyección de dependencias.</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class PublicoControlador {

    private final ServicioGrado servicioGrado;

    @GetMapping("/")
    public String inicio(Model modelo) {
        modelo.addAttribute("menuActivo", "inicio");
        return "publico/inicio";
    }

    /** Muestra los grados que ofrece el colegio (dato real leído de la base por medio del servicio). */
    @GetMapping("/grados")
    public String grados(Model modelo) {
        List<GradoDTO> grados = servicioGrado.listar();
        modelo.addAttribute("grados", grados);
        modelo.addAttribute("menuActivo", "grados");
        return "publico/grados";
    }

    @GetMapping("/servicios")
    public String servicios(Model modelo) {
        modelo.addAttribute("menuActivo", "servicios");
        return "publico/servicios";
    }

    @GetMapping("/contacto")
    public String contacto(Model modelo) {
        modelo.addAttribute("menuActivo", "contacto");
        return "publico/contacto";
    }
}
