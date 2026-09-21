package com.club.fitnessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.RegistroAccesoDTO;
import com.club.fitnessclub.services.ServicioAcceso;

import lombok.RequiredArgsConstructor;

/**
 * Controlador de RECEPCIÓN: registro de entradas y salidas por DNI (ADMIN y RECEPCION).
 *
 * <pre>
 *   GET  /accesos          -> accesos/recepcion (formulario, personas dentro y últimos movimientos)
 *   POST /accesos/entrada  -> registra la entrada del DNI informado
 *   POST /accesos/salida   -> registra la salida del DNI informado
 * </pre>
 *
 * <p>Tras cada operación se redirige a /accesos y se pasa por FLASH el DTO del movimiento
 * ("ultimoAcceso") para mostrar la FOTO y los datos de la persona: la foto sirve para que
 * recepción verifique visualmente la identidad de quien ingresa.
 */
@Controller
@RequestMapping("/accesos")
@RequiredArgsConstructor
public class ControladorAcceso extends ControladorBase {

    private final ServicioAcceso servicioAcceso;

    @GetMapping
    public String recepcion(Model model) {
        try {
            model.addAttribute("dentro", servicioAcceso.findAdentro());
            model.addAttribute("movimientos", servicioAcceso.findUltimosMovimientos());
        } catch (Exception e) {
            error(model, e);
        }
        return "accesos/recepcion";
    }

    @PostMapping("/entrada")
    public String entrada(@RequestParam String dni, RedirectAttributes ra) {
        try {
            RegistroAccesoDTO acceso = servicioAcceso.registrarEntrada(dni);
            ra.addFlashAttribute("ultimoAcceso", acceso);
            ra.addFlashAttribute("tipoUltimoAcceso", "ENTRADA");
            ok(ra, "Entrada registrada: " + acceso.getPersonaNombre() + ".");
            if (acceso.getAdvertencia() != null) {
                ra.addFlashAttribute("mensajeAdvertencia", acceso.getAdvertencia());
            }
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/accesos";
    }

    @PostMapping("/salida")
    public String salida(@RequestParam String dni, RedirectAttributes ra) {
        try {
            RegistroAccesoDTO acceso = servicioAcceso.registrarSalida(dni);
            ra.addFlashAttribute("ultimoAcceso", acceso);
            ra.addFlashAttribute("tipoUltimoAcceso", "SALIDA");
            ok(ra, "Salida registrada: " + acceso.getPersonaNombre() + " (permaneció " + acceso.getPermanencia() + ").");
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/accesos";
    }
}
