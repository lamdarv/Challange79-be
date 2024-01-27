package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Mengecek apakah ada user dengan email tertentu
    boolean existsByEmail(String email);

    // Mencari user berdasarkan email
    Optional<User> findByEmail(String email);

    // Mencari user berdasarkan user ID
    Optional<User> findById(UUID userId);
}

