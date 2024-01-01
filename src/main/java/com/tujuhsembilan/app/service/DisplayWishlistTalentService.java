package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentPosition;
import com.tujuhsembilan.app.model.TalentSkillset;
import com.tujuhsembilan.app.model.TalentWishlist;
import com.tujuhsembilan.app.repository.DisplayWishlistTalentRepository;
import com.tujuhsembilan.app.repository.PositionRepository;
import com.tujuhsembilan.app.repository.SkillsetRepository;
import com.tujuhsembilan.app.repository.TalentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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


    public Page<DisplayWishlistTalentDTO> getAllWishlistTalentsByClientId(UUID clientId, boolean isActive, Pageable pageable) {
        Page<TalentWishlist> talentWishlists = displayWishlistTalentRepository.findByClientIdAndIsActive(clientId,true ,pageable); // Metode baru
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
            dto.setTalentLevel(talent.getTalentLevelId().getTalentLevelName());

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


//    public List<DisplayWishlistTalentDTO> getAllWishlistTalents(){
//        List<TalentWishlist> talentWishlists = displayWishlistTalentRepository.findAll();
//        List<DisplayWishlistTalentDTO> displayWishlistTalentDTOs = new ArrayList<>();
//
//        for (TalentWishlist talentWishlist : talentWishlists){
//            DisplayWishlistTalentDTO dto = new DisplayWishlistTalentDTO();
//            dto.setWishlistId(talentWishlist.getTalentWishlistId());
//            dto.setTalentId(talentWishlist.getTalentId());
//
//            Talent talent = talentRepository.findById(talentWishlist.getTalentId()).orElse(null);
//            if (talent != null) {
//                dto.setTalentName(talent.getTalentName());
//                dto.setTalentAvailability(talent.getTalentAvailability());
//                dto.setTalentExperience(talent.getTalentExperience());
//                dto.setTalentLevel(talent.getTalentLevelId().getTalentLevelName());
//
//                List<PositionDTO> positionDTOs = new ArrayList<>();
//                for (TalentPosition talentPosition : talent.getTalentPositions()) {
//                    if (talentPosition != null && talentPosition.getPosition() != null) {
//                        PositionDTO positionDTO = new PositionDTO();
//                        positionDTO.setPositionId(talentPosition.getPosition().getPositionId());
//                        positionDTO.setPositionName(talentPosition.getPosition().getPositionName());
//                        positionDTOs.add(positionDTO);
//                    }
//                }
//                dto.setPositions(positionDTOs);
//
//                List<SkillsetDTO> skillsetDTOs = new ArrayList<>();
//                for (TalentSkillset talentSkillset : talent.getTalentSkillsets()) {
//                    if (talentSkillset != null && talentSkillset.getSkillset() != null){
//                        SkillsetDTO skillsetDTO = new SkillsetDTO(
//                                talentSkillset.getSkillset().getSkillsetId(),
//                                talentSkillset.getSkillset().getSkillsetName()
//                        );
//                        skillsetDTOs.add(skillsetDTO);
//                    }
//                }
//                dto.setSkillsets(skillsetDTOs);
//            }
//            displayWishlistTalentDTOs.add(dto);
//        }
//        return displayWishlistTalentDTOs;
//    }
}
