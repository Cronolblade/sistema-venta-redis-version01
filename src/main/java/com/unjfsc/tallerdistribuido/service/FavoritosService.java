package com.unjfsc.tallerdistribuido.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FavoritosService {

	private final StringRedisTemplate redisMasterTemplate;

	// --- MEJORA ---
	// Inyectamos explícitamente 'redisMasterTemplate' porque la gestión de
	// favoritos
	// (añadir, quitar, leer) debe ir siempre contra el nodo principal para
	// asegurar la consistencia de los datos del usuario.
	public FavoritosService(@Qualifier("redisMasterTemplate") StringRedisTemplate redisMasterTemplate) {
		this.redisMasterTemplate = redisMasterTemplate;
	}

	private String getFavoritosKey(String username) {
		return "favoritos:" + username;
	}

	public void agregarFavorito(String username, String productoId) {
		redisMasterTemplate.opsForSet().add(getFavoritosKey(username), productoId);
	}

	public void eliminarFavorito(String username, String productoId) {
		redisMasterTemplate.opsForSet().remove(getFavoritosKey(username), productoId);
	}

	public Set<String> getProductosFavoritos(String username) {
		return redisMasterTemplate.opsForSet().members(getFavoritosKey(username));
	}

	public boolean esFavorito(String username, String productoId) {
		return redisMasterTemplate.opsForSet().isMember(getFavoritosKey(username), productoId);
	}
}