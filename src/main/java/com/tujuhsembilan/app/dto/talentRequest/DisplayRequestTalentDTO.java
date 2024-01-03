package com.tujuhsembilan.app.dto.talentRequest;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DisplayRequestTalentDTO {
    private UUID talentRequestId;
    private UUID talentId;
    private String talentName;
    private Boolean talentAvailability;
    private Integer talentExperience;
    private String talentLevel;
    private LocalDateTime talentRequestDate;
    private String talentRequestStatus;
    private List<PositionDTO> positions;
    private List<SkillsetDTO> skillsets;
}
