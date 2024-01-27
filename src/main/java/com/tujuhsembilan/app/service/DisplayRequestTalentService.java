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
    private final DisplayRequestTalentRepository displayRequestTalentRepository;

    private final TalentRepository talentRepository;

    @Autowired
    public DisplayRequestTalentService(
            DisplayRequestTalentRepository displayRequestTalentRepository,
            TalentRepository talentRepository
    ){
        this.displayRequestTalentRepository = displayRequestTalentRepository;
        this.talentRepository = talentRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    @Transactional
    public Page<DisplayRequestTalentDTO> getTalentRequestByClientId(UUID clientId, Pageable pageable, String status) {
        Page<TalentRequest> talentRequests;

        // Memeriksa apakah status tidak null dan tidak kosong
        if (status != null && !status.isEmpty()) {
            // Jika status tersedia, gunakan query kustom berdasarkan clientId dan status
            talentRequests = displayRequestTalentRepository.findByTalentWishlist_Client_ClientIdAndTalentRequestStatus_TalentRequestStatusName(clientId, status, pageable);
        } else {
            // Jika status tidak tersedia, gunakan query berdasarkan clientId tanpa status
            talentRequests = displayRequestTalentRepository.findByTalentWishlist_Client_ClientId(clientId, pageable);
        }

        // Log jumlah talent requests yang diambil
        log.info("Fetched {} talent requests for client with ID: {}", talentRequests.getNumberOfElements(), clientId);

        // Log rinci jika trace level diaktifkan
        if (log.isTraceEnabled()) {
            talentRequests.forEach(talentRequest -> log.trace("Talent Request: {}", talentRequest));
        }

        // Mengonversi hasil ke DTO dan membungkusnya dalam PageImpl
        List<DisplayRequestTalentDTO> list = talentRequests.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, talentRequests.getTotalElements());
    }

    @Transactional
    private DisplayRequestTalentDTO mapToDTO(TalentRequest talentRequest) {
        DisplayRequestTalentDTO dto = new DisplayRequestTalentDTO();

        // Mengatur properti DTO berdasarkan TalentRequest
        dto.setTalentRequestId(talentRequest.getTalentRequestId());
        dto.setTalentRequestDate(talentRequest.getRequestDate());
        dto.setTalentRequestStatus(talentRequest.getTalentRequestStatus().getTalentRequestStatusName());

        // Mengambil informasi Talent dari TalentRequest
        Talent talent = talentRepository.findById(talentRequest.getTalentWishlist().getTalent().getTalentId()).orElse(null);

        // Memeriksa apakah Talent ditemukan
        if (talent != null) {
            // Mengatur properti DTO berdasarkan informasi Talent
            dto.setTalentId(talent.getTalentId());
            dto.setTalentName(talent.getTalentName());
            dto.setTalentExperience(talent.getTalentExperience());
            dto.setTalentAvailability(talent.getTalentAvailability());
            dto.setTalentLevel(talent.getTalentLevel().getTalentLevelName());

            // Mapping posisi dan skillset ke DTO
            List<PositionDTO> positionDTOs = mapPositions(talent.getTalentPositions());
            dto.setPositions(positionDTOs);

            List<SkillsetDTO> skillsetDTOs = mapSkillsets(talent.getTalentSkillsets());
            dto.setSkillsets(skillsetDTOs);
        }

        return dto;
    }

    @Transactional
    private List<PositionDTO> mapPositions(List<TalentPosition> talentPositions) {
        List<PositionDTO> positionDTOs = new ArrayList<>();

        // Iterasi melalui setiap TalentPosition
        for (TalentPosition talentPosition : talentPositions) {
            // Memeriksa keberadaan TalentPosition dan Position
            if (talentPosition != null && talentPosition.getPosition() != null) {
                // Jika keduanya ada, buat PositionDTO dan tambahkan ke list
                PositionDTO positionDTO = new PositionDTO();
                positionDTO.setPositionId(talentPosition.getPosition().getPositionId());
                positionDTO.setPositionName(talentPosition.getPosition().getPositionName());
                positionDTOs.add(positionDTO);
            }
        }
        return positionDTOs;
    }

    @Transactional
    private List<SkillsetDTO> mapSkillsets(List<TalentSkillset> talentSkillsets) {
        List<SkillsetDTO> skillsetDTOs = new ArrayList<>();

        // Iterasi melalui setiap TalentSkillset
        for (TalentSkillset talentSkillset : talentSkillsets) {
            // Memeriksa keberadaan TalentSkillset dan Skillset
            if (talentSkillset != null && talentSkillset.getSkillset() != null) {
                // Jika keduanya ada, buat SkillsetDTO dan tambahkan ke list
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