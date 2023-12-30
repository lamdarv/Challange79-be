package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    // You can add custom database queries here if needed
}
