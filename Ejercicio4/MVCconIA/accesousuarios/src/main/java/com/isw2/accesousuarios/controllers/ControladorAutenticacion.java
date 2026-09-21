package com.isw2.accesousuarios.controllers;

import com.isw2.accesousuarios.dtos.LoginDTO;
import com.isw2.accesousuarios.dtos.RegistroDTO;
import com.isw2.accesousuarios.dtos.UsuarioSesionDTO;
import com.isw2.accesousuarios.exceptions.ExcepcionAcceso;
import com.isw2.accesousuarios.exceptions.ExcepcionAcceso.Motivo;
import com.isw2.accesousuarios.services.ServicioUsuario;
import jakarta.servlet.http.HttpSession;
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
 * CAPA: CONTROLADOR (MVC) - ingreso, inicio y cierre de sesión
 * ============================================================================
 * Recibe las peticiones HTTP, valida el formato del formulario, delega en el
 * servicio y decide QUE vista mostrar o A DONDE redirigir. No contiene reglas de
 * negocio (esas están en {@link ServicioUsuario}).
 *
 * RUTAS
 *   GET  /        -> redirige a /inicio (con sesión) o a /login (sin sesión)
 *   GET  /login   -> muestra el formulario de ingreso
 *   POST /login   -> procesa el ingreso:
 *                      ok               -> guarda el usuario en sesión y va a /inicio
 *                      no registrado    -> redirige a /registro con el correo cargado
 *                      clave incorrecta -> vuelve a /login con el aviso de intentos
 *                      bloqueado        -> vuelve a /login con el aviso de bloqueo
 *   GET  /inicio  -> pantalla de bienvenida (requiere sesión)
 *   POST /logout  -> cierra la sesión
 *
 * MANEJO DE SESIÓN: como el ejercicio no incluye seguridad, el "estar logueado"
 * se resuelve con la sesión HTTP: tras el ingreso se guarda un
 * {@link UsuarioSesionDTO} en el atributo ATRIBUTO_SESION_USUARIO y /inicio
 * verifica manualmente que exista.
 *
 * ANOTACIONES DE CLASE
 *   @Controller : bean de la capa web. Sus métodos devuelven el NOMBRE de una vista
 *                 Thymeleaf ("login" -> templates/login.html) o una redirección
 *                 ("redirect:/inicio"). (@RestController, en cambio, devolvería JSON.)
 *   @RequiredArgsConstructor (Lombok): constructor para inyección por constructor
 *                 de los campos final (ver ServicioUsuario).
 */
@Controller
@RequiredArgsConstructor
public class ControladorAutenticacion {

    /** Nombre del atributo de sesión donde se guarda el usuario logueado (lo lee también la vista). */
    public static final String ATRIBUTO_SESION_USUARIO = "usuarioLogueado";

    private final ServicioUsuario servicioUsuario;

    /**
     * @GetMapping("/") : responde a GET / . Es solo un despachador.
     * HttpSession     : Spring inyecta la sesión HTTP del visitante.
     */
    @GetMapping("/")
    public String raiz(HttpSession session) {
        return session.getAttribute(ATRIBUTO_SESION_USUARIO) != null
                ? "redirect:/inicio"
                : "redirect:/login";
    }

    /**
     * Muestra el formulario de ingreso. Si ya hay sesión iniciada, no tiene sentido
     * mostrarlo y se envía a /inicio.
     *
     * Model: contenedor de datos que se entrega a la vista. Se agrega un LoginDTO
     * vacio (th:object del formulario) salvo que ya exista uno.
     */
    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session) {
        if (session.getAttribute(ATRIBUTO_SESION_USUARIO) != null) {
            return "redirect:/inicio";
        }
        if (!model.containsAttribute("login")) {
            model.addAttribute("login", new LoginDTO());
        }
        return "login";
    }

    /**
     * Procesa el formulario de ingreso.
     *
     * @PostMapping("/login")        : responde a POST /login (envio del formulario).
     * @Valid                        : ejecuta las validaciones del LoginDTO.
     * @ModelAttribute("login")      : enlaza los campos del formulario con el DTO y
     *                                 lo deja en el modelo con el nombre "login".
     * BindingResult                 : recoge los errores de validación. DEBE ir justo
     *                                 después del parametro validado; si faltara,
     *                                 Spring lanzaría una excepción ante un error.
     * RedirectAttributes            : permite pasar datos a la página destino de una
     *                                 redirección (flash attributes: viven una sola
     *                                 petición, patron Post-Redirect-Get).
     */
    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("login") LoginDTO login,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            UsuarioSesionDTO usuario = servicioUsuario.autenticar(login);
            session.setAttribute(ATRIBUTO_SESION_USUARIO, usuario);
            return "redirect:/inicio";

        } catch (ExcepcionAcceso e) {
            if (e.getMotivo() == Motivo.NO_REGISTRADO) {
                // Usuario inexistente: se le solicita registrarse, con el correo ya cargado.
                RegistroDTO precargado = new RegistroDTO();
                precargado.setCorreo(login.getCorreo());
                redirectAttributes.addFlashAttribute("registro", precargado);
                redirectAttributes.addFlashAttribute("aviso", e.getMessage());
                return "redirect:/registro";
            }
            // Clave incorrecta o cuenta bloqueada: se informa en el propio formulario.
            bindingResult.reject("login.error", e.getMessage());
            return "login";

        } catch (Exception e) {
            bindingResult.reject("login.error", "No se pudo completar el ingreso. Intente nuevamente.");
            return "login";
        }
    }

    /**
     * Pantalla de bienvenida. Sin sesión iniciada se redirige al login (única ruta
     * protegida del sistema, verificada a mano).
     */
    @GetMapping("/inicio")
    public String inicio(Model model, HttpSession session) {
        UsuarioSesionDTO usuario = (UsuarioSesionDTO) session.getAttribute(ATRIBUTO_SESION_USUARIO);
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "inicio";
    }

    /** Cierra la sesión (se invalida la sesión HTTP) y vuelve al login. Se invoca por POST. */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
