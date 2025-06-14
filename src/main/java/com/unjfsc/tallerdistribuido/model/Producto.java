package com.unjfsc.tallerdistribuido.model;

import lombok.Data;

@Data // Anotación de Lombok para generar getters, setters, etc.
public class Producto {
	private String id;
	private String name;
	private double price;
	private int stock;
}
