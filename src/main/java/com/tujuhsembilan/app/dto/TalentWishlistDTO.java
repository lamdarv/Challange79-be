package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TalentWishlistDTO {
    private UUID talentId;

    public UUID getTalentId() {
        return talentId;
    }

    public void setTalentId(UUID talentId) {
        this.talentId = talentId;
    }
}
