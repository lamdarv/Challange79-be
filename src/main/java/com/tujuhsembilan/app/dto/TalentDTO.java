    package com.tujuhsembilan.app.dto;

    import lombok.Data;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.UUID;

    @Data
    public class TalentDTO {
        private UUID talentId;
        private String talentPhotoUrl;
        private String talentName;
        private String talentStatus;
        //employeeStatus
        private Boolean talentAvailability;
        private Integer talentExperience;
        private String talentLevelName;
        private List<PositionDTO> positions = new ArrayList<>();
        private List<SkillsetDTO> skillsets;

        public void setSkillsets(List<SkillsetDTO> skillsets) {
            this.skillsets = skillsets;
        }

        //skillSet
        private Boolean isActive;
    }



