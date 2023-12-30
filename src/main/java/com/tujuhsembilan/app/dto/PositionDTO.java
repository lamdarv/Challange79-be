package com.tujuhsembilan.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PositionDTO {
    private UUID positionId;
    private String positionName;

    public PositionDTO(UUID positionId, String positionName) {
        this.positionId = positionId;
        this.positionName = positionName;
    }
}
