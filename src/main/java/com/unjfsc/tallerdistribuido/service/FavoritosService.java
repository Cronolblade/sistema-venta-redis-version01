package com.unjfsc.tallerdistribuido.service;

import com.unjfsc.tallerdistribuido.model.Producto;
import com.unjfsc.tallerdistribuido.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoritosService {

    @Autowired
    @Qualifier("redisMasterTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    private String getFavoritesKey(String username) {
        return "favorites:" + username;
    }

    public void agregarFavorito(String username, String productoId) {
        redisTemplate.opsForSet().add(getFavoritesKey(username), productoId);
    }

    public void eliminarFavorito(String username, String productoId) {
        redisTemplate.opsForSet().remove(getFavoritesKey(username), productoId);
    }

    public Set<Producto> getProductosFavoritos(String username) {
        Set<String> productoIds = redisTemplate.opsForSet().members(getFavoritesKey(username));
        if (productoIds == null || productoIds.isEmpty()) {
            return Set.of(); // Devuelve un conjunto vacío si no hay favoritos
        }
        
        // Busca todos los productos por sus IDs
        return productoIds.stream()
                .map(id -> productoRepository.findById(id).orElse(null))
                .filter(producto -> producto != null)
                .collect(Collectors.toSet());
    }
}