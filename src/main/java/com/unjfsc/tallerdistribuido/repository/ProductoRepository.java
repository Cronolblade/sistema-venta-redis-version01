package com.unjfsc.tallerdistribuido.repository;

import com.unjfsc.tallerdistribuido.model.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends CrudRepository<Producto, String> {
    // Spring Data Redis creará automáticamente los métodos para buscar, guardar, eliminar, etc.
}