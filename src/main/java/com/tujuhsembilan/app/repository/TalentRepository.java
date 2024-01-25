package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.dto.TalentSearchDTO;
import com.tujuhsembilan.app.model.Talent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentRepository extends JpaRepository<Talent, UUID>, JpaSpecificationExecutor<Talent> {
    Optional<Talent> findByTalentId(UUID talentId);

    // Add this method for debugging
    @Query("SELECT t FROM Talent t LEFT JOIN FETCH t.talentLevel tl WHERE tl.talentLevelName = :#{#searchDTO.talentLevelName}")
    String getGeneratedQuery(@Param("searchDTO") TalentSearchDTO searchDTO);

    @Query(value = "SELECT t.* FROM Talent t JOIN Talent_Level tl ON t.talent_level_id = tl.talent_level_id " +
            "ORDER BY CASE " +
            "WHEN tl.talent_level_name = 'Junior' THEN 1 " +
            "WHEN tl.talent_level_name = 'Middle' THEN 2 " +
            "WHEN tl.talent_level_name = 'Senior' THEN 3 END",
            nativeQuery = true)
    Page<Talent> findAllTalentsSortedByLevel(Pageable pageable);
}
