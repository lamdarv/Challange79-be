package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisplayTalentApprovalDTO {
    private UUID talentRequestId;
    private String agencyName;
    private LocalDateTime requestDate;
    private String talentName;
    private String approvalStatus;
}
