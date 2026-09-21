package com.club.fitnessclub.controllers;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.CuotaDTO;
import com.club.fitnessclub.dtos.GeneracionCuotasDTO;
import com.club.fitnessclub.dtos.PagoDTO;
import com.club.fitnessclub.enums.MedioPago;
import com.club.fitnessclub.services.ServicioCuota;
import com.club.fitnessclub.services.ServicioGrupoFamiliar;

import jakarta.validation.Valid;

/**
 * Controlador de CUOTAS del club (solo ADMIN): emisión individual y masiva, edición, anulación y
 * detalle con el formulario para registrar pagos (el pago en sí lo procesa ControladorPago).
 *
 * <pre>
 *   GET  /cuotas                 -> cuotas/abm
 *   GET  /cuotas/nueva           -> cuotas/form      (emitir cuota a una familia)
 *   GET  /cuotas/generar         -> cuotas/generar   (emitir a todas las familias)
 *   POST /cuotas/generar         -> emisión masiva
 *   GET  /cuotas/{id}            -> cuotas/detalle   (pagos + formulario de pago)
 *   GET  /cuotas/{id}/editar     -> cuotas/form
 *   POST /cuotas/guardar         -> alta / modificación
 *   POST /cuotas/{id}/anular     -> anulación
 * </pre>
 */
@Controller
@RequestMapping("/cuotas")
public class ControladorCuota extends ControladorBase {

    private final ServicioCuota servicioCuota;
    private final ServicioGrupoFamiliar servicioGrupo;
    private final BigDecimal importeSugerido;

    public ControladorCuota(ServicioCuota servicioCuota, ServicioGrupoFamiliar servicioGrupo,
                            @Value("${club.cuota.importe-sugerido:25000.00}") BigDecimal importeSugerido) {
        this.servicioCuota = servicioCuota;
        this.servicioGrupo = servicioGrupo;
        this.importeSugerido = importeSugerido;
    }

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("cuotas", servicioCuota.findAll());
        } catch (Exception e) {
            error(model, e);
        }
        return "cuotas/abm";
    }

    @GetMapping("/nueva")
    public String nueva(Model model, RedirectAttributes ra) {
        try {
            CuotaDTO dto = new CuotaDTO();
            YearMonth mes = YearMonth.now();
            dto.setPeriodo(mes);
            dto.setImporte(importeSugerido);
            dto.setFechaVencimiento(mes.atDay(10));
            model.addAttribute("cuota", dto);
            model.addAttribute("familias", servicioGrupo.findAll());
            return "cuotas/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/cuotas";
        }
    }

    @GetMapping("/generar")
    public String generar(Model model) {
        GeneracionCuotasDTO dto = new GeneracionCuotasDTO();
        YearMonth mes = YearMonth.now();
        dto.setPeriodo(mes);
        dto.setImporte(importeSugerido);
        dto.setFechaVencimiento(mes.atDay(10));
        model.addAttribute("generacion", dto);
        return "cuotas/generar";
    }

    @PostMapping("/generar")
    public String generarMasivo(@Valid @ModelAttribute("generacion") GeneracionCuotasDTO dto, BindingResult resultado,
                                Model model, RedirectAttributes ra) {
        if (resultado.hasErrors()) {
            return "cuotas/generar";
        }
        try {
            int creadas = servicioCuota.generarParaTodas(dto);
            ok(ra, creadas == 0
                    ? "No se generaron cuotas: todas las familias ya tenían la cuota de ese período."
                    : "Se generaron " + creadas + " cuotas.");
            return "redirect:/cuotas";
        } catch (Exception e) {
            error(model, e);
            return "cuotas/generar";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            CuotaDTO cuota = servicioCuota.findById(id);
            model.addAttribute("cuota", cuota);

            // DTO del formulario "Registrar pago", con el saldo como monto sugerido.
            PagoDTO pago = new PagoDTO();
            pago.setCuotaId(id);
            pago.setMonto(cuota.getSaldo());
            model.addAttribute("pago", pago);
            model.addAttribute("mediosPago", MedioPago.values());
            return "cuotas/detalle";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/cuotas";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("cuota", servicioCuota.findById(id));
            model.addAttribute("familias", servicioGrupo.findAll());
            return "cuotas/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/cuotas";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cuota") CuotaDTO dto, BindingResult resultado,
                          Model model, RedirectAttributes ra) {
        try {
            if (resultado.hasErrors()) {
                model.addAttribute("familias", servicioGrupo.findAll());
                return "cuotas/form";
            }
            if (dto.getId() == 0) {
                servicioCuota.save(dto);
                ok(ra, "Cuota emitida correctamente.");
            } else {
                servicioCuota.update(dto.getId(), dto);
                ok(ra, "Cuota actualizada correctamente.");
            }
            return "redirect:/cuotas";
        } catch (Exception e) {
            error(model, e);
            try {
                model.addAttribute("familias", servicioGrupo.findAll());
            } catch (Exception ignorada) {
                log.warn("No se pudo recargar la lista de familias", ignorada);
            }
            return "cuotas/form";
        }
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable long id, RedirectAttributes ra) {
        try {
            servicioCuota.delete(id);
            ok(ra, "Cuota anulada correctamente.");
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/cuotas";
    }
}
