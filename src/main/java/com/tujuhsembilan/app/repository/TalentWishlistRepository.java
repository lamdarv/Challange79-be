package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentWishlistRepository extends JpaRepository<TalentWishlist, UUID> {
    Optional<TalentWishlist> findById(UUID id);
    Optional<Talent> findTalentNameByTalentId(UUID talentId);
}
