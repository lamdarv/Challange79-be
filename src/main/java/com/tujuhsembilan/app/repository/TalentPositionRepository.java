package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalentPositionRepository extends JpaRepository<TalentPosition, TalentPosition.TalentPositionId> {
}
