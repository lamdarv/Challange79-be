package com.tujuhsembilan.app.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SkillsetDTO {
    private UUID tagsId;
    private String tagsName;

    public SkillsetDTO(UUID tagsId, String tagsName) {
        this.tagsId = tagsId;
        this.tagsName = tagsName;
    }
}
