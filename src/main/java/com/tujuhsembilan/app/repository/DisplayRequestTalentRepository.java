package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO;
import com.tujuhsembilan.app.model.TalentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DisplayRequestTalentRepository extends JpaRepository<TalentRequest, UUID> {

    @Query("SELECT new com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO(" +
            "tr.talentRequestId, " +
            "t.talentId, " +
            "t.talentName, " +
            "t.talentAvailability, " +
            "tm.talentExperience, " +
            "tl.talentLevelName, " +
            "tr.requestDate, " +
            "trs.talentRequestStatusName) " +
            "FROM TalentRequest tr " +
            "JOIN tr.talentRequestStatus trs " +
            "JOIN tr.talentWishlist tw " +
            "JOIN tw.talent t " +
            "LEFT JOIN t.talentMetadata tm " +
            "LEFT JOIN t.talentLevelId tl " +
            "WHERE tw.clientId = :clientId")
    Page<DisplayRequestTalentDTO> getDisplayRequestTalents(
            @Param("clientId") UUID clientId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT new com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO(" +
            "tr.talentRequestId, " +
            "t.talentId, " +
            "t.talentName, " +
            "t.talentAvailability, " +
            "tm.talentExperience, " +
            "tl.talentLevelName, " +
            "tr.requestDate, " +
            "trs.talentRequestStatusName) " +
            "FROM TalentRequest tr " +
            "JOIN tr.talentRequestStatus trs " +
            "JOIN tr.talentWishlist tw " +
            "JOIN tw.talent t " +
            "LEFT JOIN t.talentMetadata tm " +
            "LEFT JOIN t.talentLevelId tl " +
            "WHERE tw.clientId = :clientId")
    Page<DisplayRequestTalentDTO> getDisplayRequestTalentsAllStatus(
            @Param("clientId") UUID clientId,
            Pageable pageable
    );
}

