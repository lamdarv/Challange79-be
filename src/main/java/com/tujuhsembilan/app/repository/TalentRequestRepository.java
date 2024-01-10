package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TalentRequestRepository extends JpaRepository<TalentRequest, UUID> {

    // Query to find TalentRequests by talent name, ignoring case, with pagination
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCase(String talentName, Pageable pageable);

    // Query to find TalentRequests by a list of statuses, with pagination
    Page<TalentRequest> findByTalentRequestStatus_TalentRequestStatusNameIn(List<String> statusFilters, Pageable pageable);

    // Query to find TalentRequests by both talent name (ignoring case) and a list of statuses, with pagination
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseAndTalentRequestStatus_TalentRequestStatusNameIn(
            String talentName, List<String> statusFilters, Pageable pageable);

    // New Query to find by agencyName
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseOrTalentWishlist_Client_AgencyNameContainingIgnoreCase(
            String talentName, String agencyName, Pageable pageable);
}
