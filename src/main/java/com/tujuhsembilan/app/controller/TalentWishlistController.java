package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.RemoveWishlistTalentDTO;
import com.tujuhsembilan.app.dto.TalentWishlistDTO;
import com.tujuhsembilan.app.model.Client;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentWishlist;
import com.tujuhsembilan.app.model.User;
import com.tujuhsembilan.app.repository.TalentRepository;
import com.tujuhsembilan.app.repository.TalentWishlistRepository;
import com.tujuhsembilan.app.repository.UserRepository;
//import com.tujuhsembilan.app.service.ClientService;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
//import java.util.logging.Logger;

@RestController
@RequestMapping("/talent-management")
public class TalentWishlistController {

    private final UserRepository userRepository;
    private final TalentRepository talentRepository;
    private final TalentWishlistRepository talentWishlistRepository;
    private final JwtUtils jwtUtils;
    @Autowired
    private TalentWishlistService talentWishlistService;

    @Autowired
    private DisplayWishlistTalentService displayWishlistTalentService;

    private static final Logger log = LoggerFactory.getLogger(TalentWishlistController.class);

    @Autowired
    public TalentWishlistController(UserRepository userRepository,
                                    TalentRepository talentRepository,
                                    TalentWishlistRepository talentWishlistRepository,
                                    JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.talentRepository = talentRepository;
        this.talentWishlistRepository = talentWishlistRepository;
        this.jwtUtils = jwtUtils;
    }

    //POST Add Talent To Wishlist
    @PostMapping("/talents/add-to-list")
    @Transactional
    public ResponseEntity<String> addTalentToWishlist(@RequestBody TalentWishlistDTO talentWishlistDTO,
                                                      HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            if (userId == null) {
                return new ResponseEntity<>("Invalid token.", HttpStatus.UNAUTHORIZED);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            UUID clientId = user.getClient().getClientId();

            // Find the Talent entity
            Talent talent = talentRepository.findById(talentWishlistDTO.getTalentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talent not found"));

            TalentWishlist talentWishlist = TalentWishlist.builder()
                    .talent(talent)
                    .clientId(clientId)
                    .wishlistDate(LocalDateTime.now())
                    .isActive(true)
                    .build();

            talentWishlistRepository.save(talentWishlist);

            return ResponseEntity.ok(talent.getTalentName() + " with id " + talent.getTalentId() + " successfully added to wishlist!");

        } catch (Exception e) {
            return new ResponseEntity<>("Error adding talent to wishlist! " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest servletRequest) {
        String token = jwtUtils.extractTokenFromRequest(servletRequest);
        return (token != null && jwtUtils.validateToken(token)) ? jwtUtils.getUserIdFromToken(token) : null;
    }

    //GET Display Daftar Wishlist Talent By User Id
    @GetMapping("/wishlists")
    @Transactional
    public ResponseEntity<Page<DisplayWishlistTalentDTO>> getWishlistTalentsByUserId(
            @RequestParam UUID user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        log.info("Looking up wishlist talents for user ID: {}", user_id);
        Optional<User> optionalUser = userRepository.findById(user_id);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            Client client = user.getClient();

            if (client != null){
                UUID clientId = client.getClientId();
                log.info("Client ID: {}", clientId);

                Pageable pageable = PageRequest.of(page, size);
                Page<DisplayWishlistTalentDTO> result = displayWishlistTalentService.getAllWishlistTalentsByClientId(clientId, true, pageable);

                return ResponseEntity.ok(result);
            } else {
                log.warn("No client associated with user ID: {}", user_id);
                return ResponseEntity.notFound().build(); // atau return appropriate error response
            }
        } else {
            log.warn("User not found for ID: {}", user_id);
            return ResponseEntity.notFound().build();
        }
    }

    //POST Remove Wishlist
    @PostMapping("/wishlists/remove")
    @Transactional
    public ResponseEntity<String> removeWishlist(@RequestBody RemoveWishlistTalentDTO request) {
        try {
            talentWishlistService.removeWishlist(request.getTalentWishlistId());
            return ResponseEntity.ok("Wishlist item successfully deactivated");
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
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                UUID clientId = user.getClient().getClientId();

                // Deactivate all wishlists for the given clientId
                talentWishlistService.removeAllWishlist(clientId);

                return ResponseEntity.ok("All wishlists successfully deactivated for user ID: " + userId);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deactivating wishlists");
        }
    }
}
