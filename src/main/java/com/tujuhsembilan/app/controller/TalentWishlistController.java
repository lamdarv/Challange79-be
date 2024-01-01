package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.RemoveWishlistTalentDTO;
import com.tujuhsembilan.app.dto.TalentWishlistDTO;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentWishlist;
import com.tujuhsembilan.app.model.User;
import com.tujuhsembilan.app.repository.*;
import com.tujuhsembilan.app.service.DisplayWishlistTalentService;
import com.tujuhsembilan.app.service.TalentWishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

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

    //GET Display Daftar Wishlist Talent
    @GetMapping("/wishlists")
    @Transactional
    public ResponseEntity<Page<DisplayWishlistTalentDTO>> getAllWishlistTalents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DisplayWishlistTalentDTO> result = displayWishlistTalentService.getAllWishlistTalents(pageable);

        return ResponseEntity.ok(result);
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
}
