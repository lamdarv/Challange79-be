package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentRequestStatusRepository extends JpaRepository<TalentRequestStatus, UUID> {
    Optional<TalentRequestStatus> findByTalentRequestStatusName(String statusName);
}
