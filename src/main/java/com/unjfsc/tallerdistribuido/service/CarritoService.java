package com.unjfsc.tallerdistribuido.service;

import com.unjfsc.tallerdistribuido.model.Producto;
import com.unjfsc.tallerdistribuido.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CarritoService {

	@Autowired
	@Qualifier("redisMasterTemplate")
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private ProductoRepository productoRepository;

	private String getCartKey(String username) {
		return "cart:" + username;
	}

	public void agregarProducto(String username, String productoId, int cantidad) {
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

		if (producto.getStock() < cantidad) {
			throw new InsufficientStockException("Stock insuficiente para el producto: " + producto.getName());
		}

		String cartKey = getCartKey(username);
		redisTemplate.opsForHash().increment(cartKey, productoId, cantidad);
	}

	public void eliminarProducto(String username, String productoId) {
		String cartKey = getCartKey(username);
		redisTemplate.opsForHash().delete(cartKey, productoId);
	}

	public Map<Producto, Integer> getProductosEnCarrito(String username) {
		String cartKey = getCartKey(username);
		Map<Object, Object> items = redisTemplate.opsForHash().entries(cartKey);

		return items.entrySet().stream().map(entry -> {
			Producto p = productoRepository.findById((String) entry.getKey()).orElse(null);
			if (p != null) {
				return Map.entry(p, Integer.parseInt((String) entry.getValue()));
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Transactional
	public void realizarCompra(String username) {
		Map<Producto, Integer> productosEnCarrito = getProductosEnCarrito(username);

		if (productosEnCarrito.isEmpty()) {
			throw new IllegalStateException("El carrito está vacío.");
		}

		// 1. Validar stock de TODOS los productos ANTES de descontar
		for (Map.Entry<Producto, Integer> entry : productosEnCarrito.entrySet()) {
			Producto producto = entry.getKey();
			Integer cantidadPedida = entry.getValue();
			if (producto.getStock() < cantidadPedida) {
				throw new InsufficientStockException("Stock insuficiente para '" + producto.getName()
						+ "'. Disponible: " + producto.getStock() + ", pedido: " + cantidadPedida);
			}
		}

		// 2. Si hay stock para todo, proceder a descontar
		for (Map.Entry<Producto, Integer> entry : productosEnCarrito.entrySet()) {
			Producto producto = entry.getKey();
			Integer cantidadPedida = entry.getValue();
			producto.setStock(producto.getStock() - cantidadPedida);
			productoRepository.save(producto);
		}

		// 3. Vaciar el carrito del usuario
		redisTemplate.delete(getCartKey(username));
	}
}