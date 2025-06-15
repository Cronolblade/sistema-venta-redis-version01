package com.unjfsc.tallerdistribuido.model.controller;

import com.unjfsc.tallerdistribuido.model.Producto;
import com.unjfsc.tallerdistribuido.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/productos")
public class VentasController {

	private final StringRedisTemplate redisReplicaTemplate;
	private final ProductoRepository productoRepository;

	public VentasController(@Qualifier("redisReplicaTemplate") StringRedisTemplate redisReplicaTemplate,
			ProductoRepository productoRepository) {
		this.redisReplicaTemplate = redisReplicaTemplate;
		this.productoRepository = productoRepository;
	}

	// Verificador con más paciencia (hasta 5 segundos) y más logs
	private void verifyKeyReplication(String key, boolean shouldExist) {
		// Intentaremos 25 veces con pausas de 200ms = 5 segundos de espera total
		for (int i = 0; i < 25; i++) {
			try {
				Boolean keyExistsInReplica = redisReplicaTemplate.hasKey(key);
				System.out.println("Intento " + (i + 1) + ": Verificando existencia de '" + key
						+ "'. ¿Debería existir? " + shouldExist + ". ¿Existe en la réplica? " + keyExistsInReplica);

				if (keyExistsInReplica != null && keyExistsInReplica == shouldExist) {
					System.out.println("INFO: Replicación de existencia de clave '" + key
							+ "' verificada en el intento " + (i + 1));
					return;
				}
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (Exception e) {
				System.err.println("ERROR durante la verificación: " + e.getMessage());
				return;
			}
		}
		System.err.println("WARN: No se pudo verificar la replicación de existencia para la clave " + key
				+ " después de 25 intentos.");
	}

	// Verificador de Stock con más paciencia (hasta 5 segundos) y más logs
	private void verifyStockReplication(String key, int expectedStock) {
		for (int i = 0; i < 25; i++) {
			try {
				String stockStr = (String) redisReplicaTemplate.opsForHash().get(key, "stock");
				System.out.println("Intento " + (i + 1) + ": Verificando stock de '" + key + "'. Stock esperado: "
						+ expectedStock + ". Stock en réplica: " + (stockStr != null ? stockStr : "null"));

				if (stockStr != null) {
					int replicaStock = Integer.parseInt(stockStr);
					if (replicaStock == expectedStock) {
						System.out.println("INFO: Replicación de stock para '" + key + "' verificada con valor "
								+ expectedStock + " en el intento " + (i + 1));
						return;
					}
				}
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (Exception e) {
				System.err.println("ERROR durante la verificación de stock: " + e.getMessage());
				return;
			}
		}
		System.err.println("WARN: No se pudo verificar la replicación del stock para la clave " + key
				+ " después de 25 intentos.");
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<Producto> getAllProductos() {
		return (List<Producto>) productoRepository.findAll();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createProducto(@RequestBody Producto producto) {
		try {
			Producto savedProducto = productoRepository.save(producto);
			// <-- CAMBIO: Se usa "Producto" con mayúscula para coincidir con
			// @RedisHash("Producto")
			String redisKey = "Producto:" + savedProducto.getId();
			verifyKeyReplication(redisKey, true);
			return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al crear el producto: " + e.getMessage());
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateProducto(@PathVariable String id, @RequestBody Producto productoDetails) {
		Optional<Producto> optionalProducto = productoRepository.findById(id);
		if (optionalProducto.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		try {
			productoDetails.setId(id);
			Producto updatedProducto = productoRepository.save(productoDetails);
			// <-- CAMBIO: Se usa "Producto" con mayúscula para coincidir con
			// @RedisHash("Producto")
			String redisKey = "Producto:" + updatedProducto.getId();
			verifyStockReplication(redisKey, updatedProducto.getStock());
			return ResponseEntity.ok(updatedProducto);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al actualizar el producto: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteProducto(@PathVariable String id) {
		if (!productoRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		try {
			// <-- CAMBIO: Se usa "Producto" con mayúscula para coincidir con
			// @RedisHash("Producto")
			String redisKey = "Producto:" + id;
			productoRepository.deleteById(id);
			verifyKeyReplication(redisKey, false);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al eliminar el producto: " + e.getMessage());
		}
	}
}