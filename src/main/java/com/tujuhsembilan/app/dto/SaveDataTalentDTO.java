package com.tujuhsembilan.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SaveDataTalentDTO {
    private MultipartFile talentPhoto;
    private MultipartFile talentCV;
    private String talentName;
    private UUID talentStatusId;
    private String nip;
    private String sex;
    private LocalDateTime dob;
    private String talentDescription;
    private int talentExperience;
    private UUID talentLevelId;
    private int projectCompleted;
    private List<PositionDTO> position;
    private List<SkillsetDTO> skillSet;
    private String email;
    private String cellphone;
    private UUID employeeStatusId;
    private boolean talentAvailability;
    private String videoUrl;
}
