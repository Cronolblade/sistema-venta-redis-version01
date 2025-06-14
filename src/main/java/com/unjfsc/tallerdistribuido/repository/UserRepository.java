package com.unjfsc.tallerdistribuido.repository;

import com.unjfsc.tallerdistribuido.model.User;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findByUsername(String username);
}