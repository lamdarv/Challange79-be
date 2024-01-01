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
//    @Query("SELECT t.talentName FROM TalentWishlist w JOIN w.talent t WHERE t.talentId = :talentId")
//    Optional<String> findTalentNameByTalentId(@Param("talentId") UUID talentId);

    // Get daftar berdasarkan client_id
    Page<TalentWishlist> findByClientIdAndIsActive(UUID clientId, boolean isActive, Pageable pageable);
}







