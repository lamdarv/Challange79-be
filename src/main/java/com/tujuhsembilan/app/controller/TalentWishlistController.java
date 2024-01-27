package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.RemoveWishlistTalentDTO;
import com.tujuhsembilan.app.dto.TalentWishlistDTO;
import com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO;
import com.tujuhsembilan.app.dto.talentRequest.WishlistRequestDTO;
import com.tujuhsembilan.app.dto.talentRequest.WishlistResponseDTO;
import com.tujuhsembilan.app.exception.UserNotFoundException;
import com.tujuhsembilan.app.model.Client;
import com.tujuhsembilan.app.model.User;
import com.tujuhsembilan.app.repository.UserRepository;
import com.tujuhsembilan.app.service.DisplayRequestTalentService;
import com.tujuhsembilan.app.service.DisplayWishlistTalentService;
import com.tujuhsembilan.app.service.TalentWishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/talent-management")
public class TalentWishlistController {

    private final UserRepository userRepository;

    private final TalentWishlistService talentWishlistService;

    private final DisplayWishlistTalentService displayWishlistTalentService;

    private final DisplayRequestTalentService displayRequestTalentService;

    private static final Logger log = LoggerFactory.getLogger(TalentWishlistController.class);

    @Autowired
    public TalentWishlistController(UserRepository userRepository,
                                    TalentWishlistService talentWishlistService,
                                    DisplayWishlistTalentService displayWishlistTalentService,
                                    DisplayRequestTalentService displayRequestTalentService) {
        this.userRepository = userRepository;
        this.talentWishlistService = talentWishlistService;
        this.displayWishlistTalentService = displayWishlistTalentService;
        this.displayRequestTalentService = displayRequestTalentService;
    }

    //POST Add Talent To Wishlist
    @PostMapping("/talents/add-to-list")
    @Transactional
    public ResponseEntity<String> addTalentToWishlist(@RequestBody TalentWishlistDTO talentWishlistDTO,
                                                      HttpServletRequest request) {
        try {
            String result = talentWishlistService.addTalentToWishlist(talentWishlistDTO, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding talent to wishlist! " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //GET Display Daftar Wishlist Talent By User Id
    @GetMapping("/wishlists")
    @Transactional
    public ResponseEntity<Page<DisplayWishlistTalentDTO>> getWishlistTalentsByUserId(
            @RequestParam UUID user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Looking up wishlist talents for user ID: {}", user_id);
        return userRepository.findById(user_id)
                .map(user -> displayWishlistTalentService.getWishlistTalentsByUser(user, page, size))
                .orElse(ResponseEntity.notFound().build());
    }

    //POST Remove Wishlist
    @PostMapping("/wishlists/remove")
    @Transactional
    public ResponseEntity<String> removeWishlist(@RequestBody RemoveWishlistTalentDTO request) {
        try {
            UUID talentWishlistId = request.getTalentWishlistId();
            talentWishlistService.removeWishlist(talentWishlistId);
            return ResponseEntity.ok("Wishlist with id " + talentWishlistId + " successfully removed!");

        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        }
    }

    //POST Remove All Wishlist
    @PostMapping("/wishlists/remove-all")
    @Transactional
    public ResponseEntity<String> removeAllWishlist(@RequestParam UUID userId){
        try {
            talentWishlistService.removeAllWishlistByUserId(userId);
            return ResponseEntity.ok("All wishlists successfully deactivated for user ID: " + userId);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deactivating wishlists");
        }
    }

    //POST Request Talent
    @PostMapping("/wishlists/request")
    @Transactional
    public ResponseEntity<WishlistResponseDTO> createWishlistRequest(@RequestBody WishlistRequestDTO request) {
        try {
            UUID userId = request.getUserId();
            WishlistResponseDTO response = talentWishlistService.handleNewWishlistRequest(request.getWishlist());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new WishlistResponseDTO(null, "An error occurred while processing the request."));
        }
    }

    @GetMapping("/requests")
    @Transactional
    public ResponseEntity<Page<DisplayRequestTalentDTO>> getTalentRequestByUserId(
            @RequestParam UUID user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status){

        log.info("Looking up talents request for user ID: {} with status: {}", user_id, status);
        Optional<User> optionalUser = userRepository.findById(user_id);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            Client client = user.getClient();

            if (client != null){
                UUID clientId = client.getClientId();
                log.info("Client ID: {}", clientId);

                Pageable pageable = PageRequest.of(page, size);
                Page<DisplayRequestTalentDTO> result = displayRequestTalentService.getTalentRequestByClientId(clientId, pageable, status);

                return ResponseEntity.ok(result);
            } else {
                log.warn("No client associated with user ID: {}", user_id);
                return ResponseEntity.notFound().build();
            }
        } else {
            log.warn("User not found for ID: {}", user_id);
            return ResponseEntity.notFound().build();
        }
    }
}
