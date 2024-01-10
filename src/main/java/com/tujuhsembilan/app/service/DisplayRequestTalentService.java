package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentPosition;
import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentSkillset;
import com.tujuhsembilan.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisplayRequestTalentService {
    @Autowired
    private DisplayRequestTalentRepository displayRequestTalentRepository;

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SkillsetRepository skillsetRepository;

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    public Page<DisplayRequestTalentDTO> getTalentRequestByClientId(UUID clientId, Pageable pageable, String status){
        Page<TalentRequest> talentRequests;

        if (status != null && !status.isEmpty()) {
            talentRequests = displayRequestTalentRepository.findByTalentWishlist_Client_ClientIdAndTalentRequestStatus_TalentRequestStatusName(clientId, status, pageable);
        } else {
            talentRequests = displayRequestTalentRepository.findByTalentWishlist_Client_ClientId(clientId, pageable);
        }

        log.info("Fetched {} talent requests for client with ID: {}", talentRequests.getNumberOfElements(), clientId);

        if (log.isTraceEnabled()) {
            talentRequests.forEach(talentRequest -> log.trace("Talent Request: {}", talentRequest));
        }

        List<DisplayRequestTalentDTO> list = talentRequests.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, talentRequests.getTotalElements());
    }

    private DisplayRequestTalentDTO mapToDTO(TalentRequest talentRequest) {
        DisplayRequestTalentDTO dto = new DisplayRequestTalentDTO();
        dto.setTalentRequestId(talentRequest.getTalentRequestId());
        dto.setTalentRequestDate(talentRequest.getRequestDate());
        dto.setTalentRequestStatus(talentRequest.getTalentRequestStatus().getTalentRequestStatusName());

        Talent talent = talentRepository.findById(talentRequest.getTalentWishlist().getTalent().getTalentId()).orElse(null);
        if (talent != null) {
            dto.setTalentId(talent.getTalentId());
            dto.setTalentName(talent.getTalentName());
            dto.setTalentExperience(talent.getTalentExperience());
            dto.setTalentAvailability(talent.getTalentAvailability());
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