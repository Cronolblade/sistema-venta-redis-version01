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

	/**
	 * Registra un nuevo usuario a partir de un objeto User. Lanza una excepción si
	 * el nombre de usuario ya está en uso.
	 * 
	 * @param user El objeto User con el nombre de usuario y la contraseña sin
	 *             codificar.
	 */
	public void registerNewUser(User user) {
		// 1. Verificar si el usuario ya existe
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			throw new IllegalStateException("El nombre de usuario '" + user.getUsername() + "' ya está en uso.");
		}

		// 2. Crear el nuevo usuario
		User newUser = new User();
		newUser.setUsername(user.getUsername());
		newUser.setPassword(passwordEncoder.encode(user.getPassword())); // 3. Codificar la contraseña
		newUser.setRoles(Set.of("ROLE_USER")); // 4. Asignar rol por defecto

		// 5. Guardar el usuario en Redis
		userRepository.save(newUser);
	}
}