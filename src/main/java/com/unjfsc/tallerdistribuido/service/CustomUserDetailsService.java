package com.unjfsc.tallerdistribuido.service;

import com.unjfsc.tallerdistribuido.model.User;
import com.unjfsc.tallerdistribuido.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("--- INTENTANDO AUTENTICAR AL USUARIO: " + username + " ---"); // Línea de depuración

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    // Esta sección se ejecuta si el usuario NO se encuentra en Redis
                    System.out.println("--- ERROR: Usuario '" + username + "' NO ENCONTRADO en la base de datos. ---");
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // Esta sección se ejecuta si el usuario SÍ se encuentra
        System.out.println("--- USUARIO ENCONTRADO: " + user.getUsername() + " ---");
        System.out.println("--- CONTRASEÑA HASHEADA DESDE REDIS: " + user.getPassword() + " ---");
        System.out.println("--- ROLES: " + user.getRoles() + " ---");


        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }

}