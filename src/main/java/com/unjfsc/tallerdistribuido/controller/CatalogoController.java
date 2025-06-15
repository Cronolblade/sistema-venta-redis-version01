package com.unjfsc.tallerdistribuido.controller;

import com.unjfsc.tallerdistribuido.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/catalogo")
    public String verCatalogo(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "catalogo";
    }
}