package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.talentRequest.WishlistItemDTO;
import com.tujuhsembilan.app.dto.talentRequest.WishlistResponseDTO;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentRequestStatus;
import com.tujuhsembilan.app.model.TalentWishlist;
import com.tujuhsembilan.app.repository.TalentRepository;
import com.tujuhsembilan.app.repository.TalentRequestRepository;
import com.tujuhsembilan.app.repository.TalentRequestStatusRepository;
import com.tujuhsembilan.app.repository.TalentWishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TalentWishlistService {
    @Autowired
    private TalentWishlistRepository talentWishlistRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private TalentRequestRepository talentRequestRepository;

    @Autowired
    private TalentRequestStatusRepository talentRequestStatusRepository;

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

    @Transactional
    public WishlistResponseDTO handleNewWishlistRequest(UUID userId, List<WishlistItemDTO> wishlistItems){
        List<UUID> failedRequests = new ArrayList<>();
//        String message = "All wishlists processed successfully.";

        for (WishlistItemDTO item : wishlistItems) {
            TalentWishlist wishlist = talentWishlistRepository.findById(item.getWishlistId())
                    .orElseThrow(() -> new EntityNotFoundException("Wishlist not found for ID: " + item.getWishlistId()));
            if (wishlist != null && wishlist.getTalent().isAvailable()){
                // Update is_active in TalentWishlist to false
                wishlist.deactivate();
                talentWishlistRepository.save(wishlist);

                // Update talent_availability in Talent to false
                Talent talent = wishlist.getTalent();
                talent.setAvailability(false);
                talentRepository.save(talent);

                // Create and save a new talent request
                TalentRequestStatus onProgressStatus = talentRequestStatusRepository
                        .findByTalentRequestStatusName("On Progress")
                        .orElseThrow(() -> new IllegalStateException("On Progress status not found"));

                // Insert data into talent_request
                TalentRequest talentRequest = new TalentRequest();
                talentRequest.setRequestDate(LocalDateTime.now());
                talentRequest.setTalentRequestStatus(onProgressStatus);
                talentRequestRepository.save(talentRequest);
            } else {
                failedRequests.add(item.getWishlistId());
            }
        }

        String message = failedRequests.isEmpty() ?
                "All talent requests processed successfully." :
                "Some talent requests failed to process.";

        return new WishlistResponseDTO(failedRequests, message);
    }
}
