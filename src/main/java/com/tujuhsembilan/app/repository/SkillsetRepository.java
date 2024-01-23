package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Skillset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.naming.Name;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillsetRepository extends JpaRepository<Skillset, String> {
    @Query("SELECT s FROM Skillset s WHERE LOWER(s.skillsetName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Skillset> findByNameContainsIgnoreCase(@Param("name") String name);

    Optional<Skillset> findBySkillsetName(String name);

    List<Skillset> findAllBySkillsetTypeId(UUID skillsetTypeId);
}
