package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TalentStatusRepository extends JpaRepository<TalentStatus, UUID> {
}
