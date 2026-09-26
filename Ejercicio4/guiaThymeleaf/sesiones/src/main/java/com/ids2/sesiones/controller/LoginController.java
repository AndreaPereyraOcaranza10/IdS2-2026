package com.ids2.sesiones.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    //Base de Datos "lógica"
    private final Map<String, String> usuarios = new HashMap<>();

    public LoginController() {
        usuarios.put("MaxCaufield", "1234");
        usuarios.put("LaraCroft", "1234");
        usuarios.put("AlexChen", "1234");
        usuarios.put("ChloePrice", "1234");
    }
    //Login
    //get: mostrar el login
    @GetMapping("/login")
    public String mostrarLogin(){
        return "login";
    }

    //Inicio de sesión
    //post: enviar desde el login el usuario y la contraseña, luego redirecciona
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String nombreUsuario,
                                @RequestParam String password,
                                HttpSession session,
                                Model model){

        //validar que el usuario exista
        if (usuarios.containsKey(nombreUsuario)){
            //validar contraseña
            String contra = usuarios.get(nombreUsuario);
            if (contra.equals(password)){
                session.setAttribute("usuarioLogueado", nombreUsuario);
                return "redirect:/bienvenida";
            }
        }
        //si no existe el usuario o si falla la contraseña
        model.addAttribute("error", "Usuario o contraseña incorrecto");

        return "login";


    }

    //Página de bienvenida
    @GetMapping("/bienvenida")
    public String mostrarBienvenida(HttpSession session){

        String usuario = (String) session.getAttribute("usuarioLogueado");

        if (usuario==null){
            return "redirect:/login";

        }

        return "bienvenida";

    }

    //Cerrar sesión
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session){
        session.invalidate();
        return "redirect:/login";

    }

}
