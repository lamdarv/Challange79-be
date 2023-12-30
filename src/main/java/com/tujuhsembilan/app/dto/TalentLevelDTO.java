package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TalentLevelDTO {
    private UUID talentLevelId;
    private String talentLevelName;

    public TalentLevelDTO(UUID talentLevelId, String talentLevelName) {
        this.talentLevelId = talentLevelId;
        this.talentLevelName = talentLevelName;
    }
}
