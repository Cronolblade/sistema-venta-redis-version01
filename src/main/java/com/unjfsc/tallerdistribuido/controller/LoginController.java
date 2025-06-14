package com.unjfsc.tallerdistribuido.controller;

import com.unjfsc.tallerdistribuido.service.UserService; // Importar el servicio
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // Inyectar el servicio
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // --- NUEVOS MÉTODOS PARA EL REGISTRO ---

    @GetMapping("/registro")
    public String showRegistrationForm() {
        return "registro"; // Devuelve la vista registro.html
    }

    @PostMapping("/registro")
    public String processRegistration(@RequestParam("username") String username, @RequestParam("password") String password) {
        boolean success = userService.registerNewUser(username, password);

        if (success) {
            return "redirect:/login?registro_exitoso"; // Redirige al login con un mensaje de éxito
        } else {
            return "redirect:/registro?error"; // Redirige de vuelta al registro con un mensaje de error
        }
    }
}