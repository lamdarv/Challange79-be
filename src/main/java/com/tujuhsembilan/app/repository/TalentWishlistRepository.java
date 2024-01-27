package com.tujuhsembilan.app.repository;

import com.tujuhsembilan.app.model.Client;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentWishlistRepository extends JpaRepository<TalentWishlist, UUID> {
    // Metode query JPA untuk mencari objek Talent berdasarkan talentID dari tabel TalentWishlist.
    @Query("SELECT t FROM TalentWishlist w JOIN w.talent t WHERE t.talentId = :talentId")
    Optional<Talent> findTalentByTalentId(@Param("talentId") UUID talentId);

    // Metode query JPA untuk mencari daftar objek TalentWishlist berdasarkan clientId dari tabel TalentWishlist.
    List<TalentWishlist> findAllByClient_ClientId(UUID clientId);

    // Metode query JPA untuk menonaktifkan objek TalentWishlist berdasarkan talentWishlistId di tabel TalentWishlist.
    @Modifying
    @Query("UPDATE TalentWishlist w SET w.isActive = false WHERE w.talentWishlistId = :talentWishlistId")
    void deactivateWishlist(@Param("talentWishlistId") UUID talentWishlistId);


    // Metode untuk mencari objek TalentWishlist berdasarkan talent dan client
    TalentWishlist findByTalentAndClient(Talent talent, Client client);

}
