package com.unjfsc.tallerdistribuido.controller;

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
import java.util.Map;
import java.util.Set;

@Controller
public class CatalogoController {

	private final StringRedisTemplate redisReplicaTemplate;
	private final FavoritosService favoritosService;
	private final ObjectMapper objectMapper;

	public CatalogoController(@Qualifier("redisReplicaTemplate") StringRedisTemplate redisReplicaTemplate,
			FavoritosService favoritosService, ObjectMapper objectMapper) {
		this.redisReplicaTemplate = redisReplicaTemplate;
		this.favoritosService = favoritosService;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/catalogo")
	public String verCatalogo(Model model, Authentication authentication) {
		String username = authentication.getName();

		// --- CAMBIO 1: Buscar claves con "P" mayúscula ---
		// Ahora buscamos "Producto:*" para que coincida con lo que guarda el
		// administrador.
		Set<String> keys = redisReplicaTemplate.keys("Producto:*");
		List<Producto> productos = new ArrayList<>();

		if (keys != null && !keys.isEmpty()) {
			for (String key : keys) {
				try {
					// --- CAMBIO 2: Leer los datos como HASH, no como STRING ---
					// Usamos opsForHash().entries(key) para obtener todos los campos y valores del
					// producto.
					// Esto devuelve un Map<String, String> que representa el objeto Producto.
					Map<Object, Object> productoHash = redisReplicaTemplate.opsForHash().entries(key);

					// Convertimos el Map (el Hash de Redis) a un objeto Producto usando
					// ObjectMapper.
					Producto producto = objectMapper.convertValue(productoHash, Producto.class);

					// Asignamos el ID que viene de la clave de Redis, ya que no está dentro del
					// Hash.
					// Ejemplo: de la clave "Producto:123-abc", extraemos "123-abc".
					String id = key.substring("Producto:".length());
					producto.setId(id);

					productos.add(producto);

				} catch (Exception e) {
					// Manejar la excepción, por ejemplo, loguearla.
					System.err.println("Error al procesar la clave de Redis: " + key);
					e.printStackTrace();
				}
			}
		}

		// La lógica de favoritos sigue igual.
		Set<String> favoritosIds = favoritosService.getProductosFavoritos(username);
		model.addAttribute("productos", productos);
		model.addAttribute("favoritosIds", favoritosIds);

		return "catalogo";
	}
}