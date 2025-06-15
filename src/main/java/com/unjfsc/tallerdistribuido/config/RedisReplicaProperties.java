package com.unjfsc.tallerdistribuido.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Esta clase mapeará automáticamente todas las propiedades que empiecen con "redis.replica"
@Configuration
@ConfigurationProperties(prefix = "redis.replica")
public class RedisReplicaProperties {

    private String host;
    private int port;
    private String password;

    // Getters y Setters para que Spring pueda inyectar los valores
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}