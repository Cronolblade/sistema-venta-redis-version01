package com.unjfsc.tallerdistribuido.service;

import com.unjfsc.tallerdistribuido.model.User;
import com.unjfsc.tallerdistribuido.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registerNewUser(String username, String rawPassword) {
        // 1. Verificar si el usuario ya existe
        if (userRepository.findByUsername(username).isPresent()) {
            return false; // El usuario ya existe
        }

        // 2. Crear el nuevo usuario
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword)); // 3. Codificar la contraseña
        newUser.setRoles(Set.of("ROLE_USER")); // 4. Asignar rol por defecto

        // 5. Guardar el usuario en Redis
        userRepository.save(newUser);

        return true; // Registro exitoso
    }
}