package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.model.TalentWishlist;
import com.tujuhsembilan.app.repository.TalentWishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TalentWishlistService {
    @Autowired
    private TalentWishlistRepository talentWishlistRepository;

    public void removeWishlist(UUID wishlistId) {
        // Optional: Check if the wishlist item exists before deactivating
        TalentWishlist wishlistItem = talentWishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found"));

        if (!wishlistItem.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wishlist item already deactivated");
        }

        talentWishlistRepository.deactivateWishlist(wishlistId);
    }

    @Transactional
    public void removeAllWishlist(UUID clientId){
        List<TalentWishlist> wishlists = talentWishlistRepository.findAllByClientId(clientId);

        for (TalentWishlist wishlist : wishlists) {
            if(wishlist.getIsActive()){
                talentWishlistRepository.deactivateWishlist(wishlist.getTalentWishlistId());
            }
        }
    }


}
