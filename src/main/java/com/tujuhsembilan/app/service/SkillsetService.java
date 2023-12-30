package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.MostFrequentSkillsetDTO;
import com.tujuhsembilan.app.model.MostFrequentSkillset;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.repository.MostFrequentSkillsetRepository;
import com.tujuhsembilan.app.repository.SkillsetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SkillsetService {

    @Autowired
    private SkillsetRepository skillsetRepository;

    @Autowired
    private MostFrequentSkillsetRepository mostFrequentSkillsetRepository;

    @Transactional
    public MostFrequentSkillsetDTO updateOrInsertTag(String tagName) {
        Skillset skillsetEntity = skillsetRepository.findBySkillsetName(tagName)
                .orElseGet(() -> {
                    Skillset newSkillset = new Skillset();
                    newSkillset.setSkillsetName(tagName);
                    newSkillset.setIsActive(true); // assuming new tags are active by default
                    // Save the new Skillset to generate skillset_id
                    return skillsetRepository.save(newSkillset);
                });

        MostFrequentSkillset mostFrequentSkillsetEntity = mostFrequentSkillsetRepository
                .findBySkillset(skillsetEntity)
                .orElseGet(() -> {
                    MostFrequentSkillset newMostFrequentSkillset = new MostFrequentSkillset();
                    newMostFrequentSkillset.setSkillset(skillsetEntity);
                    newMostFrequentSkillset.setCounter(1);
                    return newMostFrequentSkillset;
                });

        if (mostFrequentSkillsetEntity.getMostFrequentSkillsetId() != null) {
            // If it's an existing MostFrequentSkillset, just increment the counter
            mostFrequentSkillsetEntity.setCounter(mostFrequentSkillsetEntity.getCounter() + 1);
        }
        // Save the MostFrequentSkillset to the database
        mostFrequentSkillsetEntity = mostFrequentSkillsetRepository.save(mostFrequentSkillsetEntity);

        // Convert to DTO
        return convertToDTO(mostFrequentSkillsetEntity);
    }

    private MostFrequentSkillsetDTO convertToDTO(MostFrequentSkillset mostFrequentSkillset) {
        MostFrequentSkillsetDTO dto = new MostFrequentSkillsetDTO();
        dto.setTagsId(mostFrequentSkillset.getSkillset().getSkillsetId());
        dto.setTagsName(mostFrequentSkillset.getSkillset().getSkillsetName());
        dto.setCounter(mostFrequentSkillset.getCounter());
        return dto;
    }
}

