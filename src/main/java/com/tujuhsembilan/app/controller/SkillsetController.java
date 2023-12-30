package com.tujuhsembilan.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tujuhsembilan.app.dto.MostFrequentSkillsetDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.model.MostFrequentSkillset;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.repository.MostFrequentSkillsetRepository;
import com.tujuhsembilan.app.repository.SkillsetRepository;
import com.tujuhsembilan.app.service.SkillsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/tags-management")
@CrossOrigin(origins = "http://localhost:3000")
public class SkillsetController {
    private final SkillsetRepository skillsetRepository;
    private final MostFrequentSkillsetRepository mostFrequentSkillsetRepository;
    private final SkillsetService skillsetService;

    @Autowired
    public SkillsetController(SkillsetRepository skillsetRepository, MostFrequentSkillsetRepository mostFrequentSkillsetRepository, SkillsetService skillsetService){
        this.skillsetRepository = skillsetRepository;
        this.mostFrequentSkillsetRepository = mostFrequentSkillsetRepository;
        this.skillsetService = skillsetService;
    }

    //API GET Autocomplete Search Data Tags
    @GetMapping("/tags-option-lists")
    public List<SkillsetDTO> getTagsByName(@RequestParam("tagsName") String tagsName) {
        // Retrieve Skillsets by tagsName
        List<Skillset> skillsets = skillsetRepository.findByNameContainsIgnoreCase(tagsName);

        // Mapping Skillsets to SkillsetDTO
        List<SkillsetDTO> skillsetDTOList = skillsets.stream()
                .map(skillset -> new SkillsetDTO(skillset.getSkillsetId(), skillset.getSkillsetName()))
                .collect(Collectors.toList());

        //Notes : belum menghandle karakter khusus
        return skillsetDTOList;
    }

    //API GET Autocomplete Search Data Popular Tags
    @GetMapping("/popular-tags-option-lists")
    public List<MostFrequentSkillsetDTO> getPopularTags() {
        List<MostFrequentSkillset> popularTags = mostFrequentSkillsetRepository.findTop5ByCounterIsNotNullOrderByCounterDesc();

        return popularTags.stream()
                .map(MostFrequentSkillsetDTO::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/tags")
    public ResponseEntity<MostFrequentSkillsetDTO> updateOrInsertTag(
            @RequestParam("tagName") String tagName) {
        MostFrequentSkillsetDTO updatedTag = skillsetService.updateOrInsertTag(tagName);
        return new ResponseEntity<>(updatedTag, HttpStatus.OK);
    }
}
