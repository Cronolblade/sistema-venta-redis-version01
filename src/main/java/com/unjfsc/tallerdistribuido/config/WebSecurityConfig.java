package com.unjfsc.tallerdistribuido.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // <-- 1. IMPORTAR
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // <-- 2. HABILITAR SEGURIDAD A NIVEL DE MÉTODO
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 3. AÑADIR ESTA LÍNEA PARA DESACTIVAR CSRF PARA LA API
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) 
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/registro", "/Admin/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/ventas", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}