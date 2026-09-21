package com.club.fitnessclub.controllers;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.club.fitnessclub.dtos.ImagenRostroDTO;
import com.club.fitnessclub.dtos.PersonaDTO;
import com.club.fitnessclub.enums.Parentesco;
import com.club.fitnessclub.exceptions.ExcepcionNegocio;
import com.club.fitnessclub.services.ServicioGrupoFamiliar;
import com.club.fitnessclub.services.ServicioPersona;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador del ABM de PERSONAS (socios titulares y familiares) y de su foto de rostro.
 *
 * <pre>
 *   GET  /personas                 -> personas/abm    (ADMIN y RECEPCION)
 *   GET  /personas/{id}/rostro     -> imagen JPEG/PNG (ADMIN y RECEPCION)
 *   GET  /personas/nueva[?familiaId=] -> personas/form (solo ADMIN)
 *   GET  /personas/{id}/editar     -> personas/form   (solo ADMIN)
 *   POST /personas/guardar         -> alta/modificación multipart (persona + foto)
 *   POST /personas/{id}/eliminar   -> baja lógica
 * </pre>
 *
 * <p>La foto llega como MultipartFile (capa web) y se convierte a ImagenRostroDTO ANTES de invocar
 * al servicio, así la capa de servicio no depende de Spring MVC.
 */
@Controller
@RequestMapping("/personas")
@RequiredArgsConstructor
public class ControladorPersona extends ControladorBase {

    private final ServicioPersona servicioPersona;
    private final ServicioGrupoFamiliar servicioGrupo;

    @GetMapping
    public String listar(Model model) {
        try {
            model.addAttribute("personas", servicioPersona.findAll());
        } catch (Exception e) {
            error(model, e);
        }
        return "personas/abm";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long familiaId, Model model, RedirectAttributes ra) {
        try {
            PersonaDTO dto = new PersonaDTO();
            dto.setGrupoFamiliarId(familiaId); // preselecciona la familia si viene desde su detalle
            model.addAttribute("persona", dto);
            cargarCatalogos(model);
            return "personas/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/personas";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("persona", servicioPersona.findById(id));
            cargarCatalogos(model);
            return "personas/form";
        } catch (Exception e) {
            error(ra, e);
            return "redirect:/personas";
        }
    }

    /**
     * Alta o modificación con foto. El formulario es multipart/form-data.
     * En el alta la foto es obligatoria; en la edición es opcional (si no se elige, se conserva la actual).
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("persona") PersonaDTO dto, BindingResult resultado,
                          @RequestParam(value = "archivoRostro", required = false) MultipartFile archivo,
                          Model model, RedirectAttributes ra) {
        try {
            if (resultado.hasErrors()) {
                cargarCatalogos(model);
                return "personas/form";
            }

            ImagenRostroDTO imagen = (archivo == null || archivo.isEmpty())
                    ? null
                    : new ImagenRostroDTO(archivo.getOriginalFilename(), archivo.getContentType(), archivo.getBytes());

            if (dto.getId() == 0) {
                servicioPersona.save(dto, imagen);
                ok(ra, "Persona registrada correctamente.");
            } else {
                servicioPersona.update(dto.getId(), dto, imagen);
                ok(ra, "Persona actualizada correctamente.");
            }
            return "redirect:/personas";
        } catch (Exception e) {
            error(model, e);
            try {
                cargarCatalogos(model);
            } catch (Exception ignorada) {
                log.warn("No se pudieron recargar los catálogos del formulario", ignorada);
            }
            return "personas/form"; // el navegador no conserva el archivo elegido: hay que volver a seleccionarlo
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes ra) {
        try {
            servicioPersona.delete(id);
            ok(ra, "Persona dada de baja correctamente.");
        } catch (Exception e) {
            error(ra, e);
        }
        return "redirect:/personas";
    }

    /**
     * Sirve la foto de rostro como imagen (para usar en &lt;img src="/personas/{id}/rostro"&gt;).
     * @ResponseBody implícito: devuelve ResponseEntity con los bytes y el Content-Type real.
     * noCache: el navegador revalida siempre, así al reemplazar la foto se ve la nueva.
     */
    @GetMapping("/{id}/rostro")
    public ResponseEntity<byte[]> rostro(@PathVariable long id) {
        try {
            ImagenRostroDTO imagen = servicioPersona.obtenerRostro(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagen.getContentType()))
                    .cacheControl(CacheControl.noCache().cachePrivate())
                    .body(imagen.getDatos());
        } catch (ExcepcionNegocio e) {
            return ResponseEntity.notFound().build(); // sin foto: la vista muestra la imagen por defecto
        } catch (Exception e) {
            log.error("Error al servir el rostro de la persona {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Listas para los &lt;select&gt; del formulario: familias vigentes y valores del enum Parentesco. */
    private void cargarCatalogos(Model model) throws Exception {
        model.addAttribute("familias", servicioGrupo.findAll());
        model.addAttribute("parentescos", Parentesco.values());
    }
}
