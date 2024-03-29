package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisplayRequestTalentRepository extends JpaRepository<TalentRequest, UUID> {

    // Query untuk mencari halaman TalentRequest berdasarkan clientId, dengan paginasi
    Page<TalentRequest> findByTalentWishlist_Client_ClientId(UUID clientId, Pageable pageable);

    // Query untuk mencari halaman TalentRequest berdasarkan clientId dan nama status, dengan paginasi
    Page<TalentRequest> findByTalentWishlist_Client_ClientIdAndTalentRequestStatus_TalentRequestStatusName(
            UUID clientId, String statusName, Pageable pageable);
}


