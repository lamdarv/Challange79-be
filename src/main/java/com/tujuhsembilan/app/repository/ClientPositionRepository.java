package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.ClientPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientPositionRepository extends JpaRepository<ClientPosition, UUID> {

    // Mencari ClientPosition berdasarkan nama clientPositionName, mengembalikan Optional
    Optional<ClientPosition> findByClientPositionName(String clientPositionName);
}

