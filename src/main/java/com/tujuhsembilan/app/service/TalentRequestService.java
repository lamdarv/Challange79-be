package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentRequestStatus;
import com.tujuhsembilan.app.repository.TalentRequestRepository;
import com.tujuhsembilan.app.repository.TalentRequestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TalentRequestService {

    @Autowired
    private TalentRequestRepository talentRequestRepository;

    @Autowired
    private TalentRequestStatusRepository talentRequestStatusRepository;

    public void createOrUpdateTalentRequest(UUID talentRequestId) {
        TalentRequest talentRequest = new TalentRequest();

        // Fetch the TalentRequestStatus entity
        TalentRequestStatus onProgressStatus = talentRequestStatusRepository
                .findByTalentRequestStatusName("On Progress")
                .orElseThrow(() -> new IllegalStateException("Status 'On Progress' not found"));

        // Set the TalentRequestStatus entity in the TalentRequest
        talentRequest.setTalentRequestStatus(onProgressStatus);

        talentRequestRepository.save(talentRequest);
    }
}

