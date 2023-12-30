package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TalentRepository extends JpaRepository<Talent, UUID>, JpaSpecificationExecutor<Talent> {
    Optional<Talent> findByTalentId(UUID talentId);
}
