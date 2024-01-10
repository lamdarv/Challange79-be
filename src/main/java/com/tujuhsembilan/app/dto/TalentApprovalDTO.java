package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.util.UUID;

public class TalentApprovalDTO {
    private UUID talentRequestId;
    private String action;
    private String rejectReason;

    public UUID getTalentRequestId() {
        return talentRequestId;
    }

    public void setTalentRequestId(UUID talentRequestId) {
        this.talentRequestId = talentRequestId;
    }

    public String getAction(){
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRequestRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
