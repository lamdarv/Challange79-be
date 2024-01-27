package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentLevelRepository extends JpaRepository<TalentLevel, UUID> {

    // Query untuk mencari nama talent level berdasarkan talentLevelId
    @Query("SELECT tl.talentLevelName FROM TalentLevel tl WHERE tl.talentLevelId = :talentLevelId")
    Optional<String> findTalentLevelNameByTalentLevelId(@Param("talentLevelId") UUID talentLevelId);
}
