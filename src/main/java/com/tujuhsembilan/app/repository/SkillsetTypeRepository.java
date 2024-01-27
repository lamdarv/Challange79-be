package com.tujuhsembilan.app.repository;

//import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.model.SkillsetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillsetTypeRepository extends JpaRepository<SkillsetType, UUID> {
    // Mencari SkillsetType berdasarkan id, mengembalikan Optional
    Optional<SkillsetType> findById(UUID id);

    // Mencari SkillsetType berdasarkan skillsetTypeName
    SkillsetType findBySkillsetTypeName(String name);
}
