package com.club.fitnessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.PagoDTO;
import com.club.fitnessclub.services.ServicioPago;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de PAGOS de cuotas (solo ADMIN).
 *
 * <pre>
 *   GET  /pagos               -> pagos/abm  (historial de pagos)
 *   POST /pagos/registrar     -> registra un pago (Efectivo / Transferencia / Mercado Pago)
 *   POST /pagos/{id}/anular   -> anula un pago (los pagos no se editan)
 * </pre>
 * Registrar y anular redirigen de vuelta al detalle de la cuota desde la que se operó.
 */
@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class ControladorPago extends ControladorBase {

    private final ServicioPago servicioPago;

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("pagos", servicioPago.findAll());
        } catch (Exception e) {
            error(model, e);
        }
        return "pagos/abm";
    }

    @PostMapping("/registrar")
    public String registrar(@Valid @ModelAttribute("pago") PagoDTO dto, BindingResult resultado, RedirectAttributes ra) {
        // Sin cuota no hay a dónde volver.
        if (dto.getCuotaId() == null) {
            ra.addFlashAttribute("mensajeError", "Falta indicar la cuota a pagar.");
            return "redirect:/cuotas";
        }
        String destino = "redirect:/cuotas/" + dto.getCuotaId();

        if (resultado.hasErrors()) {
            ra.addFlashAttribute("mensajeError", erroresDe(resultado));
            return destino;
        }
        try {
            PagoDTO pago = servicioPago.registrar(dto);
            ok(ra, "Pago de $" + pago.getMonto() + " registrado por " + pago.getMedioPago().getEtiqueta() + ".");
        } catch (Exception e) {
            error(ra, e);
        }
        return destino;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable long id, @RequestParam(required = false) Long volverACuota, RedirectAttributes ra) {
        try {
            servicioPago.anular(id);
            ok(ra, "Pago anulado correctamente.");
        } catch (Exception e) {
            error(ra, e);
        }
        return volverACuota != null ? "redirect:/cuotas/" + volverACuota : "redirect:/pagos";
    }
}
