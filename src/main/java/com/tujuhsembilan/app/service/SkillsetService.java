package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.MostFrequentSkillsetDTO;
import com.tujuhsembilan.app.model.MostFrequentSkillset;
import com.tujuhsembilan.app.model.Skillset;
import com.tujuhsembilan.app.repository.MostFrequentSkillsetRepository;
import com.tujuhsembilan.app.repository.SkillsetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillsetService {
    private final SkillsetRepository skillsetRepository;
    private final MostFrequentSkillsetRepository mostFrequentSkillsetRepository;

    @Autowired
    public SkillsetService(
            SkillsetRepository skillsetRepository,
            MostFrequentSkillsetRepository mostFrequentSkillsetRepository
    ){
       this.skillsetRepository = skillsetRepository;
       this.mostFrequentSkillsetRepository = mostFrequentSkillsetRepository;
    }

    @Transactional
    public MostFrequentSkillsetDTO updateOrInsertTag(String tagName) {
        // Mencari Skillset berdasarkan nama, atau membuat Skillset baru jika tidak ditemukan
        Skillset skillsetEntity = skillsetRepository.findBySkillsetName(tagName)
                .orElseGet(() -> {
                    Skillset newSkillset = new Skillset();
                    newSkillset.setSkillsetName(tagName);
                    newSkillset.setIsActive(true);

                    // Menyimpan Skillset baru untuk menghasilkan skillset_id
                    return skillsetRepository.save(newSkillset);
                });

        // Mencari MostFrequentSkillset berdasarkan Skillset, atau membuat baru jika tidak ditemukan
        MostFrequentSkillset mostFrequentSkillsetEntity = mostFrequentSkillsetRepository
                .findBySkillset(skillsetEntity)
                .orElseGet(() -> {
                    MostFrequentSkillset newMostFrequentSkillset = new MostFrequentSkillset();
                    newMostFrequentSkillset.setSkillset(skillsetEntity);
                    newMostFrequentSkillset.setCounter(1);
                    return newMostFrequentSkillset;
                });

        // Jika MostFrequentSkillset sudah ada, tingkatkan counter
        if (mostFrequentSkillsetEntity.getMostFrequentSkillsetId() != null) {
            mostFrequentSkillsetEntity.setCounter(mostFrequentSkillsetEntity.getCounter() + 1);
        }

        // Menyimpan MostFrequentSkillset ke database
        mostFrequentSkillsetEntity = mostFrequentSkillsetRepository.save(mostFrequentSkillsetEntity);

        // Mengonversi ke DTO
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

