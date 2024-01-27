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

    // Query untuk mencari TalentRequests berdasarkan nama talent, dengan mengabaikan case, dengan paginasi
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCase(String talentName, Pageable pageable);

    // Query untuk mencari TalentRequests berdasarkan daftar status, dengan paginasi
    Page<TalentRequest> findByTalentRequestStatus_TalentRequestStatusNameIn(List<String> statusFilters, Pageable pageable);

    // Query untuk mencari TalentRequests berdasarkan nama talent (mengabaikan case) dan daftar status, dengan paginasi
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseAndTalentRequestStatus_TalentRequestStatusNameIn(
            String talentName, List<String> statusFilters, Pageable pageable);

    // Query baru untuk mencari berdasarkan agencyName
    Page<TalentRequest> findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseOrTalentWishlist_Client_AgencyNameContainingIgnoreCase(
            String talentName, String agencyName, Pageable pageable);
}

