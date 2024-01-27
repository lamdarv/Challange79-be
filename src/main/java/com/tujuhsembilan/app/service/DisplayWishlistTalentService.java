package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.controller.TalentWishlistController;
import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.DisplayWishlistTalentRepository;
import com.tujuhsembilan.app.repository.PositionRepository;
import com.tujuhsembilan.app.repository.SkillsetRepository;
import com.tujuhsembilan.app.repository.TalentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisplayWishlistTalentService {
    @Autowired
    private DisplayWishlistTalentRepository displayWishlistTalentRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SkillsetRepository skillsetRepository;
    private static final Logger log = LoggerFactory.getLogger(TalentWishlistController.class);


    @Transactional
    public ResponseEntity<Page<DisplayWishlistTalentDTO>> getWishlistTalentsByUser(User user, int page, int size) {
        Client client = user.getClient();

        if (client != null) {
            UUID clientId = client.getClientId();
            log.info("Client ID: {}", clientId);

            Pageable pageable = PageRequest.of(page, size);
            Page<DisplayWishlistTalentDTO> result = getAllWishlistTalentsByClientId(clientId, true, pageable);

            return ResponseEntity.ok(result);
        } else {
            log.warn("No client associated with user ID: {}", user.getUserId());
            return ResponseEntity.notFound().build();
        }
    }

    public Page<DisplayWishlistTalentDTO> getAllWishlistTalentsByClientId(UUID clientId, boolean isActive, Pageable pageable) {
        Page<TalentWishlist> talentWishlists = displayWishlistTalentRepository.findByClient_ClientIdAndIsActive(clientId,true ,pageable); // Metode baru
        List<DisplayWishlistTalentDTO> sortedList = talentWishlists.getContent()
                .stream()
                .map(this::mapToDTO)
                .sorted(Comparator.comparing(dto -> dto.getTalentLevel()))
                .collect(Collectors.toList());
        return new PageImpl<>(sortedList, pageable, talentWishlists.getTotalElements());
    }


    private DisplayWishlistTalentDTO mapToDTO(TalentWishlist talentWishlist) {
        DisplayWishlistTalentDTO dto = new DisplayWishlistTalentDTO();
        dto.setWishlistId(talentWishlist.getTalentWishlistId());
        dto.setTalentId(talentWishlist.getTalent().getTalentId());

        Talent talent = talentRepository.findById(talentWishlist.getTalent().getTalentId()).orElse(null);
        if (talent != null) {
            dto.setTalentName(talent.getTalentName());
            dto.setTalentAvailability(talent.getTalentAvailability());
            dto.setTalentExperience(talent.getTalentExperience());
            dto.setTalentLevel(talent.getTalentLevel().getTalentLevelName());

            List<PositionDTO> positionDTOs = mapPositions(talent.getTalentPositions());
            dto.setPositions(positionDTOs);

            List<SkillsetDTO> skillsetDTOs = mapSkillsets(talent.getTalentSkillsets());
            dto.setSkillsets(skillsetDTOs);
        }

        return dto;
    }

    private List<PositionDTO> mapPositions(List<TalentPosition> talentPositions) {
        List<PositionDTO> positionDTOs = new ArrayList<>();
        for (TalentPosition talentPosition : talentPositions) {
            if (talentPosition != null && talentPosition.getPosition() != null) {
                PositionDTO positionDTO = new PositionDTO();
                positionDTO.setPositionId(talentPosition.getPosition().getPositionId());
                positionDTO.setPositionName(talentPosition.getPosition().getPositionName());
                positionDTOs.add(positionDTO);
            }
        }
        return positionDTOs;
    }

    private List<SkillsetDTO> mapSkillsets(List<TalentSkillset> talentSkillsets) {
        List<SkillsetDTO> skillsetDTOs = new ArrayList<>();
        for (TalentSkillset talentSkillset : talentSkillsets) {
            if (talentSkillset != null && talentSkillset.getSkillset() != null) {
                SkillsetDTO skillsetDTO = new SkillsetDTO(
                        talentSkillset.getSkillset().getSkillsetId(),
                        talentSkillset.getSkillset().getSkillsetName()
                );
                skillsetDTOs.add(skillsetDTO);
            }
        }
        return skillsetDTOs;
    }
}