package com.unjfsc.tallerdistribuido.model.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
		// Escribir en el MAESTRO
		masterTemplate.opsForValue().set("producto:" + producto.getId(), productoJson);
		System.out.println("Producto guardado en MAESTRO (puerto 6379): " + producto.getName());
		return producto;
	}

	@GetMapping("/productos/{id}")
	public Producto getProducto(@PathVariable String id) throws Exception {
		System.out.println("Intentando leer desde RÉPLICA (puerto 6380) para el ID: " + id);
		// Intentar leer desde la RÉPLICA
		String productoJson = replicaTemplate.opsForValue().get("producto:" + id);

		if (productoJson == null) {
			System.out.println("No encontrado en la réplica, intentando en el maestro...");
			// Si no se encuentra (posible lag de replicación), leer del MAESTRO
			productoJson = masterTemplate.opsForValue().get("producto:" + id);
		}

		if (productoJson != null) {
			return objectMapper.readValue(productoJson, Producto.class);
		}
		return null;
	}

	/**
	 * NUEVO: Endpoint para listar todos los productos desde la RÉPLICA.
	 */
	@GetMapping("/productos")
	public List<Producto> getAllProductos() throws Exception {
		System.out.println("Listando todos los productos desde la RÉPLICA (puerto 6380)");
		// Leer todas las keys desde la RÉPLICA
		Set<String> keys = replicaTemplate.keys("producto:*");
		if (keys == null || keys.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> productoJsonList = replicaTemplate.opsForValue().multiGet(keys);

		return productoJsonList.stream().map(json -> {
			try {
				return objectMapper.readValue(json, Producto.class);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());
	}

	/**
	 * NUEVO: Endpoint para actualizar un producto en el MAESTRO.
	 */
	@PutMapping("/productos/{id}")
	public ResponseEntity<Producto> updateProducto(@PathVariable String id, @RequestBody Producto producto)
			throws Exception {
		String key = "producto:" + id;
		// Verificar existencia en el MAESTRO
		if (Boolean.FALSE.equals(masterTemplate.hasKey(key))) {
			return ResponseEntity.notFound().build();
		}
		producto.setId(id); // Asegurarse que el ID sea el correcto
		String productoJson = objectMapper.writeValueAsString(producto);
		// Escribir la actualización en el MAESTRO
		masterTemplate.opsForValue().set(key, productoJson);
		System.out.println("Producto actualizado en MAESTRO (puerto 6379): " + producto.getName());
		return ResponseEntity.ok(producto);
	}

	/**
	 * NUEVO: Endpoint para eliminar un producto del MAESTRO.
	 */
	@DeleteMapping("/productos/{id}")
	public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
		System.out.println("Eliminando producto del MAESTRO (puerto 6379) con ID: " + id);
		// La eliminación es una operación de escritura, va al MAESTRO
		masterTemplate.delete("producto:" + id);
		return ResponseEntity.noContent().build();
	}
}