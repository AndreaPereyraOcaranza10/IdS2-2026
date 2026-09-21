package com.club.fitnessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.club.fitnessclub.services.ServicioPanel;

import lombok.RequiredArgsConstructor;

/** Tablero (dashboard) inicial del panel de administración: contadores generales del club. */
@Controller
@RequestMapping("/panel")
@RequiredArgsConstructor
public class ControladorPanel extends ControladorBase {

    private final ServicioPanel servicioPanel;

    @GetMapping
    public String panel(Model model) {
        // "resumen" es un DTO de solo lectura con los contadores del tablero.
        model.addAttribute("resumen", servicioPanel.obtenerResumen());
        return "panel";
    }
}
