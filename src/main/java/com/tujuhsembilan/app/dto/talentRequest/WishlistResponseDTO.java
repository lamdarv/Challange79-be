package com.tujuhsembilan.app.dto.talentRequest;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class WishlistResponseDTO {
    private List<UUID> failedRequests;
    private String message;

    public WishlistResponseDTO(List<UUID> failedRequests, String message) {
        this.failedRequests = failedRequests;
        this.message = message;
    }
}
