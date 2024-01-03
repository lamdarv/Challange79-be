package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.model.Position;
import com.tujuhsembilan.app.model.TalentPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
}
