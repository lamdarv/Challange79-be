package com.tujuhsembilan.app.dto;

import com.tujuhsembilan.app.model.MostFrequentSkillset;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class MostFrequentSkillsetDTO {
    private UUID tagsId;
    private String tagsName;
    private Integer counter;

    public UUID getTagsId() {
        return tagsId;
    }

    public String getTagsName() {
        return tagsName;
    }

    public Integer getCounter(){
        return counter;
    }

    public MostFrequentSkillsetDTO(MostFrequentSkillset skillset) {
        this.tagsId = skillset.getMostFrequentSkillsetId();
        this.tagsName = skillset.getSkillset().getSkillsetName();
        this.counter = skillset.getCounter();
    }
}
