package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.TalentExperienceDTO;
import com.tujuhsembilan.app.dto.TalentLevelDTO;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.model.SkillsetType;
import com.tujuhsembilan.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MasterManagementService {
    private final PositionRepository positionRepository;
    private final TalentRepository talentRepository;
    private final TalentLevelRepository talentLevelRepository;
    private final SkillsetRepository skillsetRepository;
    private final SkillsetTypeRepository skillsetTypeRepository;

    @Autowired
    public MasterManagementService(
            PositionRepository positionRepository,
            TalentRepository talentRepository,
            TalentLevelRepository talentLevelRepository,
            SkillsetRepository skillsetRepository,
            SkillsetTypeRepository skillsetTypeRepository) {
        this.positionRepository = positionRepository;
        this.talentRepository = talentRepository;
        this.talentLevelRepository = talentLevelRepository;
        this.skillsetRepository = skillsetRepository;
        this.skillsetTypeRepository = skillsetTypeRepository;
    }

    // Master Position
    @Transactional
    public List<PositionDTO> getAllPositionOptions() {
        // Mengambil semua posisi dari repositori dan mengonversinya ke dalam List PositionDTO
        return positionRepository.findAll().stream()
                .map(position -> new PositionDTO(position.getPositionId(), position.getPositionName()))
                .collect(Collectors.toList());
    }

    // Master Experience
    @Transactional
    public List<TalentExperienceDTO> getAllExperienceOptions(int size) {
        // Membuat objek Pageable untuk mengambil halaman pertama dengan ukuran tertentu
        Pageable pageable = PageRequest.of(0, size);

        // Mengambil semua talent dari repositori dan mengonversinya ke dalam List TalentExperienceDTO
        return talentRepository.findAll(pageable).stream()
                .map(talent -> {
                    // Membuat objek TalentExperienceDTO dan mengatur propertinya berdasarkan bakat
                    TalentExperienceDTO dto = new TalentExperienceDTO();
                    dto.setExperience(talent.getTalentExperience());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Master Level
    @Transactional
    public List<TalentLevelDTO> getAllLevelOptions() {
        // Mengambil semua talent level dari repositori dan mengonversinya ke dalam List TalentLevelDTO
        return talentLevelRepository.findAll().stream()
                .map(level -> new TalentLevelDTO(level.getTalentLevelId(), level.getTalentLevelName()))
                .collect(Collectors.toList());
    }

    // Master Skillsets
    private static final Map<String, String> TYPE_MAP = Map.of(
            "1", "Framework",
            "2", "Programming Language",
            "3", "Development Tools",
            "4", "UI/UX",
            "5", "Dev-Ops",
            "6", "Documentation",
            "7", "Hardware Skills",
            "8", "Tech Support"
    );

    // Master Skillsets
    @Transactional
    public List<Skillset> getSkillsetByType(String type) {
        // Memeriksa apakah type adalah "ALL" (case-insensitive)
        if ("ALL".equalsIgnoreCase(type)) {
            // Jika ya, mengembalikan semua skillset dari repositori
            return skillsetRepository.findAll();
        } else {
            // Jika tidak, memanggil metode findSkillsetsByType dengan type yang diberikan
            return findSkillsetsByType(type);
        }
    }


    // Master Skillsets
    @Transactional
    private List<Skillset> findSkillsetsByType(String type) {
        SkillsetType skillsetType;

        // Pengecekan apakah tipe adalah UUID valid
        if (type.matches("^[0-9a-fA-F-]{36}$")) {
            // Jika ya, mencari SkillsetType berdasarkan UUID
            skillsetType = skillsetTypeRepository.findById(UUID.fromString(type)).orElse(null);
        } else {
            // Jika tidak, mencari SkillsetType berdasarkan nama tipe dari map
            String skillsetTypeName = TYPE_MAP.getOrDefault(type, type);
            skillsetType = skillsetTypeRepository.findBySkillsetTypeName(skillsetTypeName);
        }

        // Mengembalikan semua Skillset berdasarkan SkillsetType atau empty list jika SkillsetType tidak ditemukan
        return (skillsetType != null) ? skillsetRepository.findAllBySkillsetTypeId(skillsetType.getSkillsetTypeId()) : Collections.emptyList();
    }

}
