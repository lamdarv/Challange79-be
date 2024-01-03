package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.dto.talentRequest.DisplayRequestTalentDTO;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentPosition;
import com.tujuhsembilan.app.model.TalentSkillset;
import com.tujuhsembilan.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    @Autowired
    private SkillsetRequestTalentRepository skillsetRequestTalentRepository;

    @Autowired
    private PositionRequestTalentRepository positionRequestTalentRepository;

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    @Transactional(readOnly = true)
    public Page<DisplayRequestTalentDTO> getDisplayRequestTalentsWithDetails(UUID clientId, String status, Pageable pageable) {
        Page<DisplayRequestTalentDTO> talentsPage = displayRequestTalentRepository.getDisplayRequestTalents(clientId, status, pageable);

        // Process each talent DTO to enrich it with positions and skillsets
        talentsPage.getContent().forEach(talentDTO -> {
            // Fetch the Talent entity using talentId
            Talent talent = talentRepository.findById(talentDTO.getTalentId()).orElse(null);
            if (talent != null) {
                // Fetch related positions and skillsets
                List<TalentPosition> talentPositions = positionRequestTalentRepository.findByTalent(talent);
                List<PositionDTO> positions = mapPositions(talentPositions);
                talentDTO.setPositions(positions);

                List<TalentSkillset> talentSkillsets = skillsetRequestTalentRepository.findByTalent(talent);
                List<SkillsetDTO> skillsets = mapSkillsets(talentSkillsets);
                talentDTO.setSkillsets(skillsets);
            }
        });

        return talentsPage;
    }

    private List<PositionDTO> mapPositions(List<TalentPosition> talentPositions) {
        return talentPositions.stream()
                .map(tp -> new PositionDTO(tp.getPosition().getPositionId(), tp.getPosition().getPositionName()))
                .collect(Collectors.toList());
    }

    private List<SkillsetDTO> mapSkillsets(List<TalentSkillset> talentSkillsets) {
        return talentSkillsets.stream()
                .map(ts -> new SkillsetDTO(ts.getSkillset().getSkillsetId(), ts.getSkillset().getSkillsetName()))
                .collect(Collectors.toList());
    }

    private Page<DisplayRequestTalentDTO> getDisplayRequestTalent(UUID clientId, String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return displayRequestTalentRepository.getDisplayRequestTalents(clientId, status, pageable);
        } else {
            return displayRequestTalentRepository.getDisplayRequestTalentsAllStatus(clientId, pageable);
        }
    }
//    private List<PositionDTO> mapPositions(List<TalentPosition> talentPositions) {
//        List<PositionDTO> positionDTOs = new ArrayList<>();
//        for (TalentPosition talentPosition : talentPositions) {
//            if (talentPosition != null && talentPosition.getPosition() != null) {
//                PositionDTO positionDTO = new PositionDTO();
//                positionDTO.setPositionId(talentPosition.getPosition().getPositionId());
//                positionDTO.setPositionName(talentPosition.getPosition().getPositionName());
//                positionDTOs.add(positionDTO);
//            }
//        }
//        return positionDTOs;
//    }
//
//    private List<SkillsetDTO> mapSkillsets(List<TalentSkillset> talentSkillsets) {
//        List<SkillsetDTO> skillsetDTOs = new ArrayList<>();
//        for (TalentSkillset talentSkillset : talentSkillsets) {
//            if (talentSkillset != null && talentSkillset.getSkillset() != null) {
//                // Assuming that SkillsetDTO has a constructor that takes UUID and String as parameters
//                SkillsetDTO skillsetDTO = new SkillsetDTO(
//                        talentSkillset.getSkillset().getSkillsetId(),
//                        talentSkillset.getSkillset().getSkillsetName()
//                );
//                skillsetDTOs.add(skillsetDTO);
//            }
//        }
//        return skillsetDTOs;
//    }


}
