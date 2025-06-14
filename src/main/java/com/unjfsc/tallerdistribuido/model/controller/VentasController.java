package com.unjfsc.tallerdistribuido.model.controller;

import com.unjfsc.tallerdistribuido.model.Producto;
import com.unjfsc.tallerdistribuido.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/productos")
public class VentasController {

	private final ProductoRepository productoRepository;

	public VentasController(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<Producto> getAllProductos() {
		return (List<Producto>) productoRepository.findAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Producto> getProductoById(@PathVariable String id) {
		return productoRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')") // <-- CORREGIDO
	public ResponseEntity<Producto> createProducto(@RequestBody Producto producto) {
		producto.setId(UUID.randomUUID().toString());
		Producto nuevoProducto = productoRepository.save(producto);
		return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')") // <-- CORREGIDO
	public ResponseEntity<Producto> updateProducto(@PathVariable String id, @RequestBody Producto productoDetails) {
		return productoRepository.findById(id).map(productoExistente -> {
			productoExistente.setName(productoDetails.getName());
			productoExistente.setPrecio(productoDetails.getPrecio());
			productoExistente.setStock(productoDetails.getStock());
			Producto productoActualizado = productoRepository.save(productoExistente);
			return ResponseEntity.ok(productoActualizado);
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')") // <-- CORREGIDO
	public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
		if (productoRepository.existsById(id)) {
			productoRepository.deleteById(id);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}