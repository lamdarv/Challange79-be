package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.dto.TalentExperienceDTO;
import com.tujuhsembilan.app.dto.TalentLevelDTO;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.service.MasterManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master-management")
@CrossOrigin(origins = "http://localhost:3000")
public class MasterManagementController {
    private final MasterManagementService masterManagementService;

    @Autowired
    public MasterManagementController(MasterManagementService masterManagementService){
        this.masterManagementService = masterManagementService;
    }

    // Master Position
    @GetMapping("/talent-position-option-lists")
    @Transactional
    public ResponseEntity<List<PositionDTO>> getTalentPositionOptionLists() {
        List<PositionDTO> positions = masterManagementService.getAllPositionOptions();
        return ResponseEntity.ok(positions);
    }

    // Master Experience
    @GetMapping("/year-experience-option-lists")
    @Transactional
    public ResponseEntity<List<TalentExperienceDTO>> getTalentExperienceOptionLists(
            @RequestParam(defaultValue = "10") int size) {
        List<TalentExperienceDTO> experience = masterManagementService.getAllExperienceOptions(size);
        return ResponseEntity.ok(experience);
    }

    // Master Level
    @GetMapping("/talent-level-option-lists")
    @Transactional
    public ResponseEntity<List<TalentLevelDTO>> getTalentLevelOptionLists() {
        List<TalentLevelDTO> level = masterManagementService.getAllLevelOptions();
        return ResponseEntity.ok(level);
    }

    // Master Skillsets
    @GetMapping("/skill-set-option-lists")
    @Transactional
    public ResponseEntity<List<SkillsetDTO>> getSkillSetOptionLists(@RequestParam(name = "type", defaultValue = "ALL") String type) {
        List<Skillset> skillsets = masterManagementService.getSkillsetByType(type);
        List<SkillsetDTO> result = skillsets.stream()
                .map(skillset -> new SkillsetDTO(skillset.getSkillsetId(), skillset.getSkillsetName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
