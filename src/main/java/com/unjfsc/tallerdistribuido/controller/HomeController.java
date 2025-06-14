package com.unjfsc.tallerdistribuido.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Cambiamos el GetMapping para que responda a AMBAS rutas: la raíz y /ventas
    @GetMapping({"/", "/ventas"})
    public String ventas() {
        // Devuelve el nombre de la plantilla de Thymeleaf: ventas.html
        return "ventas";
    }
}