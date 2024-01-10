package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentPosition;
import com.tujuhsembilan.app.model.TalentPositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalentPositionRepository extends JpaRepository<TalentPosition, TalentPositionId> {
}
