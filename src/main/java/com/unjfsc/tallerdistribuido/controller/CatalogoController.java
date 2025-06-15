package com.unjfsc.tallerdistribuido.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unjfsc.tallerdistribuido.model.Producto;
import com.unjfsc.tallerdistribuido.service.FavoritosService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class CatalogoController {

	private final StringRedisTemplate redisReplicaTemplate;
	private final FavoritosService favoritosService;
	private final ObjectMapper objectMapper;

	// --- MEJORA ---
	// Inyectamos explícitamente el 'redisReplicaTemplate' para todas las
	// operaciones de lectura.
	// Esto asegura que no sobrecargamos al nodo maestro con solicitudes de lectura.
	public CatalogoController(@Qualifier("redisReplicaTemplate") StringRedisTemplate redisReplicaTemplate,
			FavoritosService favoritosService, ObjectMapper objectMapper) {
		this.redisReplicaTemplate = redisReplicaTemplate;
		this.favoritosService = favoritosService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/catalogo")
	public String verCatalogo(Model model, Authentication authentication) {
		String username = authentication.getName();

		// --- MEJORA ---
		// Leemos todos los productos directamente desde la RÉPLICA de Redis.
		Set<String> keys = redisReplicaTemplate.keys("producto:*");
		List<Producto> productos = new ArrayList<>();
		if (keys != null) {
			for (String key : keys) {
				String json = redisReplicaTemplate.opsForValue().get(key);
				try {
					Producto producto = objectMapper.readValue(json, Producto.class);
					productos.add(producto);
				} catch (IOException e) {
					// Manejar la excepción, por ejemplo, loguearla.
					e.printStackTrace();
				}
			}
		}

		// La lógica de favoritos sigue igual, ya que es específica del usuario.
		Set<String> favoritosIds = favoritosService.getProductosFavoritos(username);
		model.addAttribute("productos", productos);
		model.addAttribute("favoritosIds", favoritosIds);
		return "catalogo";
	}
}