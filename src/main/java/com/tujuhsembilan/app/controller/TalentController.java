package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.ProfileCounterDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.dto.TalentDTO;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/talent-management")
@CrossOrigin(origins = "http://localhost:3000")
public class TalentController {
    private final TalentRepository talentRepository;
    private final TalentWishlistRepository talentWishlistRepository;
    private final JwtUtils jwtUtils;
    private final TalentMetadataRepository talentMetadataRepository;
    private final DisplayWishlistTalentRepository displayWishlistTalentRepository;

    @Autowired
    public TalentController(TalentRepository talentRepository,
                            TalentWishlistRepository talentWishlistRepository,
                            UserRepository userRepository,
                            JwtUtils jwtUtils,
                            TalentMetadataRepository talentMetadataRepository,
                            DisplayWishlistTalentRepository displayWishlistTalentRepository) {
        this.talentRepository = talentRepository;
        this.talentWishlistRepository = talentWishlistRepository;
        this.jwtUtils = jwtUtils;
        this.talentMetadataRepository = talentMetadataRepository;
        this.displayWishlistTalentRepository = displayWishlistTalentRepository;
    }



    //DisplayTalents
    @GetMapping("/talents")
    @Transactional
    public Page<TalentDTO> getAllTalents(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "talentLevelName", required = false) String talentLevelName,
            @RequestParam(value = "talentExperience", defaultValue = "talentExperience,desc") String talentExperience,
            @RequestParam(value = "talentStatus", required = false) String talentStatus,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "talentName", required = false) String talentName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        // Create a Specification based on search criteria
        Specification<Talent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("talentName")), "%" + search.toLowerCase() + "%"));
            }
            if (talentLevelName != null && !talentLevelName.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("talentLevelId").get("talentLevelName"), talentLevelName));
            }
            if (talentStatus != null && !talentStatus.trim().isEmpty()) {
                Join<Talent, TalentStatus> talentStatusJoin = root.join("talentStatusId");
                String formattedTalentStatus = talentStatus.trim().replaceAll("\\s+", " ");
                predicates.add(cb.equal(cb.lower(talentStatusJoin.get("talentStatus")), formattedTalentStatus.toLowerCase()));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort finalSort = Sort.unsorted();

        if (talentExperience != null && !talentExperience.isEmpty()) {
            String[] talentExperienceParams = talentExperience.split(",");
            Sort talentExperienceSort = Sort.by(talentExperienceParams[0]);
            if (talentExperienceParams.length > 1) {
                talentExperienceSort = "asc".equalsIgnoreCase(talentExperienceParams[1]) ? talentExperienceSort.ascending() : talentExperienceSort.descending();
            }
            finalSort = talentExperienceSort.and(Sort.by("talentLevelId.talentLevelName").descending());
        }
        if (talentName != null && !talentName.isEmpty()) {
            String[] talentNameParams = talentName.split(",");
            Sort talentNameSort = Sort.by(talentNameParams[0]);
            talentNameSort = "asc".equalsIgnoreCase(talentNameParams[1]) ? talentNameSort.ascending() : talentNameSort.descending();
            finalSort = finalSort.and(talentNameSort);
            System.out.println("Name Sort: " + talentNameSort);
        }

        // Create a Pageable instance with sort
        Pageable pageable = PageRequest.of(page, size, finalSort);

        // Apply the Specification and Pageable to the query
        Page<Talent> talentPage = talentRepository.findAll(spec, pageable);

        // Map to DTOs and return
        return talentPage.map(this::convertToDTO);
    }


    private TalentDTO convertToDTO(Talent talent) {

        TalentDTO dto = new TalentDTO();
        dto.setTalentId(talent.getTalentId());
        dto.setTalentPhotoUrl(talent.getTalentPhotoUrl());
        dto.setTalentName(talent.getTalentName());

        if (talent.getTalentStatusId() != null) {
            dto.setTalentStatus(talent.getTalentStatusId().getTalentStatus());
        }

        dto.setTalentAvailability(talent.getTalentAvailability());
        dto.setTalentExperience(talent.getTalentExperience());

        if (talent.getTalentLevelId() != null) {
            dto.setTalentLevelName(talent.getTalentLevelId().getTalentLevelName());
        }

        if (talent.getTalentPositions() != null) {
            List<PositionDTO> positionDTOs = talent.getTalentPositions().stream()
                    .filter(Objects::nonNull)
                    .map(talentPosition -> {
                        Position position = talentPosition.getPosition();
                        if (position != null) {
                            PositionDTO positionDTO = new PositionDTO();
                            positionDTO.setPositionId(position.getPositionId());
                            positionDTO.setPositionName(position.getPositionName());
                            return positionDTO;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            dto.setPositions(positionDTOs);
        }

        if(talent.getTalentSkillsets() != null){
            List<SkillsetDTO> skillsetDTOs = talent.getTalentSkillsets().stream()
                    .filter(Objects::nonNull)
                    .map(talentSkillset -> {
                        Skillset skillset = talentSkillset.getSkillset();
                        if(skillset != null) {
                            return new SkillsetDTO(skillset.getSkillsetId(), skillset.getSkillsetName());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            dto.setSkillsets(skillsetDTOs);
        }

        dto.setIsActive(talent.getIsActive());

        return dto;
    }

    //API PUT Count Profile
    @PutMapping("/talents/profile-count")
    @Transactional
    public ResponseEntity<String> putCountProfile(@RequestBody ProfileCounterDTO profileCounterDTO){
        try {
            TalentMetadata talentMetadata = talentMetadataRepository.findById(profileCounterDTO.getTalentId())
                    .orElseThrow(() -> new EntityNotFoundException("Talent not found with id: " + profileCounterDTO.getTalentId()));

            talentMetadata.setProfileCounter(talentMetadata.getProfileCounter() + 1);
            talentMetadataRepository.save(talentMetadata);

            return ResponseEntity.ok("Counting profile with id " + talentMetadata.getTalent().getTalentId() + " is " + talentMetadata.getProfileCounter());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile counter: " + e.getMessage());
        }
    }




}
