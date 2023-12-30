package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.ClientPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientPositionRepository extends JpaRepository<ClientPosition, UUID> {
    Optional<ClientPosition> findByClientPositionName(String clientPositionName);
}
