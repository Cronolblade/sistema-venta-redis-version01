package com.unjfsc.tallerdistribuido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class SistemaVentaRedisVersion01Application {

	public static void main(String[] args) {
		SpringApplication.run(SistemaVentaRedisVersion01Application.class, args);
	}

}
