package com.unjfsc.tallerdistribuido.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash; // <-- 1. IMPORTAR

import java.io.Serializable;

@RedisHash("Producto") // <-- 2. AÑADIR ESTA ANOTACIÓN
public class Producto implements Serializable {

    @Id
    private String id;
    private String name;
    private double precio;
    private int stock;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}