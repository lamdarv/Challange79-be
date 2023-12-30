package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SkillsetTypeDTO {
    private UUID skillsetTypeId;
    private String skillsetTypeName;

    public SkillsetTypeDTO(UUID skillsetTypeId, String skillsetTypeName) {
        this.skillsetTypeId = skillsetTypeId;
        this.skillsetTypeName = skillsetTypeName;
    }
}
