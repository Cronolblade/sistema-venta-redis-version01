package com.unjfsc.tallerdistribuido.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed; // <-- ¡IMPORTAR ESTA CLASE!
import java.util.Set;

@RedisHash("User")
public class User {
    @Id
    private String id;

    @Indexed // <-- ¡AGREGAR ESTA ANOTACIÓN!
    private String username;

    private String password;
    
    private Set<String> roles;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}