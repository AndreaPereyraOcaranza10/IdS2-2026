package com.id2.thymeleafProyecto.controller;

import com.id2.thymeleafProyecto.model.Persona;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PersonaController {

    @GetMapping("/")
    public String mostrarFormulario(Model model) {
        model.addAttribute("persona", new Persona());
        return "formulario";

    }

    //llegan 3 parametros
    @PostMapping("/procesar")
    public String procesarFormulario(Persona persona, Model model) {
        /*
        //unificar nombre y apellido
        String nombreCompleto = nombre + " " + apellido;

        String tipoEdad;
        if (edad >= 18) {
            tipoEdad = "Mayor de edad";
        } else {
            tipoEdad = "Menor de edad";
        }

        // Enviamos datos a la vista
        model.addAttribute("nombreCompleto", nombreCompleto);
        model.addAttribute("edad", edad);
        model.addAttribute("tipoEdad", tipoEdad);

        //el return representa la proxima vista a la que va a saltar
        return "resultado";*/

        //actualiza los datos en el modelo
        model.addAttribute("persona", persona);
        if (persona.getEdad() >= 18) {
            model.addAttribute("tipoEdad", "Es mayor de edad");
        } else {
            model.addAttribute("tipoEdad", "Es menor de edad");
        }
        return "resultado";
    }



}