package com.unjfsc.tallerdistribuido.model.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unjfsc.tallerdistribuido.model.Producto;

@RestController
@RequestMapping("/api/ventas")
public class VentasController {

	@Autowired
	@Qualifier("redisMasterTemplate")
	private StringRedisTemplate masterTemplate;

	@Autowired
	@Qualifier("redisReplicaTemplate")
	private StringRedisTemplate replicaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@PostMapping("/productos")
	public Producto createProducto(@RequestBody Producto producto) throws Exception {
		producto.setId(UUID.randomUUID().toString());
		String productoJson = objectMapper.writeValueAsString(producto);

		masterTemplate.opsForValue().set("producto:" + producto.getId(), productoJson);
		System.out.println("Producto guardado en MAESTRO (puerto 6379): " + producto.getName());
		return producto;
	}

	@GetMapping("/productos/{id}")
	public Producto getProducto(@PathVariable String id) throws Exception {
		System.out.println("Intentando leer desde RÉPLICA (puerto 6380) para el ID: " + id);
		String productoJson = replicaTemplate.opsForValue().get("producto:" + id);

		if (productoJson == null) {
			System.out.println("No encontrado en la réplica, intentando en el maestro...");
			productoJson = masterTemplate.opsForValue().get("producto:" + id);
		}

		if (productoJson != null) {
			return objectMapper.readValue(productoJson, Producto.class);
		}
		return null;
	}
}
