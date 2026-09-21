package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.dtos.CambioPasswordDTO;
import com.colegio.sistemaescolar.exceptions.ExcepcionNegocio;
import com.colegio.sistemaescolar.services.ServicioCuenta;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CONTROLADOR de "Mi perfil" y cambio de contraseña del usuario autenticado (ADMIN o DOCENTE).
 * La URL no recibe ningún identificador de usuario: el servicio siempre opera sobre la cuenta de la sesión actual.
 */
@Controller
@RequestMapping("/panel/perfil")
@RequiredArgsConstructor
public class PerfilControlador {

    private final ServicioCuenta servicio;

    @GetMapping
    public String perfil(Model modelo) throws ExcepcionNegocio {
        modelo.addAttribute("perfil", servicio.perfil());
        modelo.addAttribute("cambio", new CambioPasswordDTO());
        modelo.addAttribute("menuActivo", "perfil");
        return "perfil/perfil";
    }

    @PostMapping("/password")
    public String cambiarPassword(@Valid @ModelAttribute("cambio") CambioPasswordDTO cambio,
                                  BindingResult resultado,
                                  Model modelo,
                                  RedirectAttributes atributos) throws ExcepcionNegocio {
        if (!resultado.hasErrors()) {
            try {
                servicio.cambiarPassword(cambio);
                atributos.addFlashAttribute("exito", "Tu contraseña se cambió correctamente.");
                return "redirect:/panel/perfil";
            } catch (ExcepcionNegocio e) {
                e.aplicarEn(resultado);
            }
        }
        cambio.setPasswordActual(null);
        cambio.setPasswordNueva(null);
        cambio.setConfirmarPassword(null);
        modelo.addAttribute("perfil", servicio.perfil());
        modelo.addAttribute("menuActivo", "perfil");
        return "perfil/perfil";
    }
}
