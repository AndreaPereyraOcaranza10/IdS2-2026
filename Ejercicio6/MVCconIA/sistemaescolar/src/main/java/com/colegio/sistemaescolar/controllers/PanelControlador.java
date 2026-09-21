package com.colegio.sistemaescolar.controllers;

import com.colegio.sistemaescolar.services.ServicioPanel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CONTROLADOR del tablero inicial del panel privado (plantilla SB Admin, Bootstrap 5).
 * Acceso: ADMIN o DOCENTE (regla en {@code ConfiguracionSeguridad}: {@code /panel/**}).
 */
@Controller
@RequiredArgsConstructor
public class PanelControlador {

    private final ServicioPanel servicioPanel;

    @GetMapping("/panel")
    public String inicio(Model modelo) {
        modelo.addAttribute("resumen", servicioPanel.obtener());
        modelo.addAttribute("menuActivo", "panel");
        return "panel/inicio";
    }
}
