package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TalentMetadataRepository extends JpaRepository<TalentMetadata, UUID> {

    // Mencari TalentMetadata berdasarkan talentId
    TalentMetadata findByTalent_TalentId(UUID talentId);
}


