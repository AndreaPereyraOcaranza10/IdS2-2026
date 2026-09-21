package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.RegistroDocenteDTO;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioDocente;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CONTROLADOR de ingreso y registro de docentes.
 *
 * <h2>Quién hace qué</h2>
 * <ul>
 *   <li>{@code GET /login}: muestra el formulario. El POST a /login NO lo atiende este controlador: lo procesa el filtro de
 *       Spring Security configurado en {@code ConfiguracionSeguridad} (usa {@code ServicioDetallesUsuario} + BCrypt).</li>
 *   <li>{@code GET/POST /registro}: autoregistro del docente (Nombre, Apellido, Sexo, Fecha de nacimiento, correo y contraseña).
 *       Al confirmarse, {@code ServicioDocente} publica el evento que dispara el correo de bienvenida.</li>
 * </ul>
 *
 * <h2>Validación en dos niveles</h2>
 * (1) {@code @Valid} aplica las anotaciones Jakarta Validation del DTO (formato, obligatorios);
 * (2) el servicio valida reglas de negocio (correo repetido, contraseñas distintas) y lanza {@link ExcepcionNegocio},
 * que acá se transforma en un error asociado al campo del formulario.
 *
 * <h2>Patrón PRG (Post-Redirect-Get)</h2>
 * Tras un POST exitoso se hace {@code redirect:} para que recargar la página no reenvíe el formulario.
 */
@Controller
@RequiredArgsConstructor
public class AutenticacionControlador {

    private final ServicioDocente servicioDocente;

    @GetMapping("/login")
    public String login(Model modelo) {
        modelo.addAttribute("menuActivo", "login");
        return "publico/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model modelo) {
        modelo.addAttribute("registro", new RegistroDocenteDTO());
        modelo.addAttribute("menuActivo", "login");
        return "publico/registro";
    }

    /**
     * {@code @ModelAttribute("registro")}: enlaza los campos del formulario al DTO y lo deja disponible en la vista con
     * ese nombre. {@link BindingResult} DEBE ir inmediatamente después del objeto validado.
     */
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroDocenteDTO registro,
                            BindingResult resultado,
                            Model modelo,
                            RedirectAttributes atributos) {
        modelo.addAttribute("menuActivo", "login");
        if (!resultado.hasErrors()) {
            try {
                servicioDocente.registrar(registro);
                atributos.addFlashAttribute("exito",
                        "¡Registro exitoso! Te enviamos un correo de bienvenida. Ya podés iniciar sesión.");
                return "redirect:/login";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        // Por seguridad, las contraseñas no se devuelven a la vista.
        registro.setPassword(null);
        registro.setConfirmarPassword(null);
        return "publico/registro";
    }
}
