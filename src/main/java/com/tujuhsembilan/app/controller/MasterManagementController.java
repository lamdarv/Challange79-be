package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.*;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.model.SkillsetType;
import com.tujuhsembilan.app.repository.PositionRepository;
import com.tujuhsembilan.app.repository.SkillsetTypeRepository;
import com.tujuhsembilan.app.repository.TalentLevelRepository;
import com.tujuhsembilan.app.repository.TalentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master-management")
@CrossOrigin(origins = "http://localhost:3000")
public class MasterManagementController {
    private final PositionRepository positionRepository;
    private final TalentRepository talentRepository;
    private final TalentLevelRepository talentLevelRepository;

    private final SkillsetTypeRepository skillsetTypeRepository;

    @Autowired
    public MasterManagementController(PositionRepository positionRepository, TalentRepository talentRepository, TalentLevelRepository talentLevelRepository, SkillsetTypeRepository skillsetTypeRepository){
        this.positionRepository = positionRepository;
        this.talentRepository = talentRepository;
        this.talentLevelRepository = talentLevelRepository;
        this.skillsetTypeRepository = skillsetTypeRepository;
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

    private List<SkillsetType> getSkillsetType(String type) {
        List<SkillsetType> skillsetTypeList;
        switch (type.toUpperCase()) {
            case "1":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Framework");
                break;
            case "2":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Programming Language");
                break;
            case "3":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Development Tools");
                break;
            case "4":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("UI/UX");
                break;
            case "5":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Dev-Ops");
                break;
            case "6":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Documentation");
                break;
            case "7":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Hardware Skills");
                break;
            case "8":
                skillsetTypeList = skillsetTypeRepository.findAllBySkillsetTypeName("Tech Support");
                break;
            default:
                skillsetTypeList = skillsetTypeRepository.findAll();
                break;
        }
        return skillsetTypeList.isEmpty() ? Collections.emptyList() : skillsetTypeList;
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

    @GetMapping("/skill-set-option-lists")
    public ResponseEntity<List<SkillsetTypeDTO>> getSkillSetOptionLists(@RequestParam(name = "type", defaultValue = "ALL") String type) {
        List<SkillsetType> skillsetType = getSkillsetType(type);
        List<SkillsetTypeDTO> result = skillsetType.stream()
                .map(st -> new SkillsetTypeDTO(st.getSkillsetTypeId(), st.getSkillsetTypeName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
