package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.*;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.model.SkillsetType;
import com.tujuhsembilan.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master-management")
@CrossOrigin(origins = "http://localhost:3000")
public class MasterManagementController {
    private final PositionRepository positionRepository;
    private final TalentRepository talentRepository;
    private final TalentLevelRepository talentLevelRepository;

    private final SkillsetTypeRepository skillsetTypeRepository;

    private final SkillsetRepository skillsetRepository;

    @Autowired
    public MasterManagementController(
            PositionRepository positionRepository,
            TalentRepository talentRepository,
            TalentLevelRepository talentLevelRepository,
            SkillsetTypeRepository skillsetTypeRepository,
            SkillsetRepository skillsetRepository){
        this.positionRepository = positionRepository;
        this.talentRepository = talentRepository;
        this.talentLevelRepository = talentLevelRepository;
        this.skillsetTypeRepository = skillsetTypeRepository;
        this.skillsetRepository = skillsetRepository;
    }

    //MasterPosition
    public List<PositionDTO> getAllPositionOptions() {
        return positionRepository.findAll().stream()
                .map(position -> new PositionDTO(position.getPositionId(), position.getPositionName()))
                .collect(Collectors.toList());
    }

    //MasterExperience
    public List<TalentExperienceDTO> getAllExperienceOptions(){
        return talentRepository.findAll().stream()
                .map(talent -> {
                    TalentExperienceDTO dto = new TalentExperienceDTO();
                    dto.setExperience(talent.getTalentExperience());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //MasterLevel
    public List<TalentLevelDTO> getAllLevelOptions() {
        return talentLevelRepository.findAll().stream()
                .map(level -> new TalentLevelDTO(level.getTalentLevelId(), level.getTalentLevelName()))
                .collect(Collectors.toList());
    }

//    private List<SkillsetType> getSkillsetType(String type) {
//        List<SkillsetType> skillsetTypeList;
//        switch (type.toUpperCase()) {
//            case "1":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Framework");
//                break;
//            case "2":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Programming Language");
//                break;
//            case "3":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Development Tools");
//                break;
//            case "4":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("UI/UX");
//                break;
//            case "5":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Dev-Ops");
//                break;
//            case "6":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Documentation");
//                break;
//            case "7":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Hardware Skills");
//                break;
//            case "8":
//                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Tech Support");
//                break;
//            default:
//                skillsetTypeList = skillsetTypeRepository.findAll();
//                break;
//        }
//        return skillsetTypeList.isEmpty() ? Collections.emptyList() : skillsetTypeList;
//    }

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

    private List<Skillset> getSkillsetByType(String type) {
        // Find the skillset type by name or ID.
        SkillsetType skillsetType = null;
        if (type.matches("^[0-9a-fA-F-]{36}$")) {
            // It's a UUID, so we find by ID.
            skillsetType = skillsetTypeRepository.findById(UUID.fromString(type)).orElse(null);
        } else {
            // It's not a UUID, so we find by name.
            skillsetType = skillsetTypeRepository.findBySkillsetTypeName(type);
        }

        // If the skillset type is found, get all skillsets with that type ID.
        if (skillsetType != null) {
            return skillsetRepository.findAllBySkillsetTypeId(skillsetType.getSkillsetTypeId());
        } else {
            // If the skillset type is not found, return an empty list.
            return Collections.emptyList();
        }
    }


    @GetMapping("/talent-position-option-lists")
    public ResponseEntity<List<PositionDTO>> getTalentPositionOptionLists() {
        List<PositionDTO> positions = getAllPositionOptions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/year-experience-option-lists")
    public ResponseEntity<List<TalentExperienceDTO>> getTalentExperienceOptionLists(
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<TalentExperienceDTO> experience = talentRepository.findAll(pageable).stream()
                .map(talent -> {
                    TalentExperienceDTO dto = new TalentExperienceDTO();
                    dto.setExperience(talent.getTalentExperience());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(experience);
    }

    @GetMapping("/talent-level-option-lists")
    public ResponseEntity<List<TalentLevelDTO>> getTalentLevelOptionLists() {
        List<TalentLevelDTO> level = getAllLevelOptions();
        return ResponseEntity.ok(level);
    }

//    @GetMapping("/skill-set-option-lists")
//    public ResponseEntity<List<SkillsetTypeDTO>> getSkillSetOptionLists(@RequestParam(name = "type", defaultValue = "ALL") String type) {
//        List<SkillsetType> skillsetType = getSkillsetType(type);
//        List<SkillsetTypeDTO> result = skillsetType.stream()
//                .map(st -> new SkillsetTypeDTO(st.getSkillsetTypeId(), st.getSkillsetTypeName()))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(result);
//    }

    @GetMapping("/skill-set-option-lists")
    public ResponseEntity<List<SkillsetDTO>> getSkillSetOptionLists(@RequestParam(name = "type", defaultValue = "ALL") String type) {
        String skillsetType = TYPE_MAP.getOrDefault(type, type); // This will get the string value from the map, or use the type as is if it's not a number

        List<Skillset> skillsets;
        if ("ALL".equalsIgnoreCase(type)) {
            skillsets = skillsetRepository.findAll(); // Assuming you have this method to find all skillsets
        } else {
            skillsets = getSkillsetByType(skillsetType); // The modified service method you'd call
        }
        List<SkillsetDTO> result = skillsets.stream()
                .map(skillset -> new SkillsetDTO(skillset.getSkillsetId(), skillset.getSkillsetName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


}
