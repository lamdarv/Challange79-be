package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PositionRequestTalentRepository extends JpaRepository<TalentPosition, TalentPosition.TalentPositionId> {
    List<TalentPosition> findByTalent(Talent talent);
}
