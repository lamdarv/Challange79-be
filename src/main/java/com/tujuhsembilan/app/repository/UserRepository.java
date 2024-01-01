package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID userId);

}
