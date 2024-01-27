package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.TalentWishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisplayWishlistTalentRepository extends JpaRepository<TalentWishlist, UUID> {

    // Query untuk mencari halaman TalentWishlist berdasarkan clientId dan isActive, dengan paginasi
    Page<TalentWishlist> findByClient_ClientIdAndIsActive(UUID clientId, boolean isActive, Pageable pageable);
}








