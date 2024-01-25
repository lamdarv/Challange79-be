package com.tujuhsembilan.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class TalentSearchDTO {
    private String search;
    private String talentLevelName;
    private Integer talentExperience;
    private String talentStatus;
    private String employeeStatus;
    private Boolean isActive;
    private String talentName;
    private List<String> tagsName;
    private int page;
    private int size;
    private String sort;

    // Constructors
    public TalentSearchDTO() {
        // Default constructor
    }
}
