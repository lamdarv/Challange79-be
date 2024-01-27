package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Skillset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.naming.Name;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillsetRepository extends JpaRepository<Skillset, String> {

    // Query untuk mencari Skillset berdasarkan nama yang mengandung string tertentu (case-insensitive)
    @Query("SELECT s FROM Skillset s WHERE LOWER(s.skillsetName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Skillset> findByNameContainsIgnoreCase(@Param("name") String name);

    // Mencari Skillset berdasarkan nama, mengembalikan Optional
    Optional<Skillset> findBySkillsetName(String name);

    // Mencari semua Skillset berdasarkan skillsetTypeId
    List<Skillset> findAllBySkillsetTypeId(UUID skillsetTypeId);
}

