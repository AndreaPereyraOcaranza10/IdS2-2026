package com.is2.PruebaThymeleaf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SaludoController {

    //indica que cuando en la url este /saludo, devolvera el html
    @GetMapping("/saludo")
    public  String saludo(@RequestParam String nombre, Model model){

        model.addAttribute("nombre", nombre);
        return "saludo";
    }

}
