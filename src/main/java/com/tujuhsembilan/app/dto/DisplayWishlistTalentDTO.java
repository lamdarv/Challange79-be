package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DisplayWishlistTalentDTO {
    private UUID wishlistId;
    private UUID talentId;
    private String talentName;
    private Boolean talentAvailability;
    private Integer talentExperience;
    private String talentLevel;
    private List<PositionDTO> positions;
    private List<SkillsetDTO> skillsets;
}
