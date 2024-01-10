package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.DisplayTalentApprovalDTO;
import com.tujuhsembilan.app.dto.TalentApprovalDTO;
import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentRequestStatus;
import com.tujuhsembilan.app.repository.TalentRequestRepository;
import com.tujuhsembilan.app.repository.TalentRequestStatusRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class TalentApprovalService {
    @Autowired
    private TalentRequestRepository talentRequestRepository;

    @Autowired
    private TalentRequestStatusRepository talentRequestStatusRepository;

    private static final Logger logger = LoggerFactory.getLogger(TalentApprovalService.class);

    @Transactional
    public String approveOrRejectTalentRequest(UUID talentRequestId, String action, String rejectReason) {
        TalentRequest talentRequest = talentRequestRepository.findById(talentRequestId)
                .orElseThrow(() -> new IllegalStateException("Talent request not found"));

        TalentRequestStatus newStatus = talentRequestStatusRepository.findByTalentRequestStatusName(
                        "approve".equalsIgnoreCase(action) ? "Approved" : "Rejected")
                .orElseThrow(() -> new IllegalStateException("Status not found"));

        if ("reject".equalsIgnoreCase(action) && (rejectReason == null || rejectReason.isEmpty())) {
            throw new IllegalArgumentException("Reject reason must be provided");
        }

        talentRequest.setTalentRequestStatus(newStatus);
        talentRequest.setLastModifiedTime(LocalDateTime.now());
        if ("reject".equalsIgnoreCase(action)) {
            talentRequest.setRequestRejectReason(rejectReason);
        }

        talentRequestRepository.save(talentRequest);

        return "Talent request with id " + talentRequest.getTalentRequestId() + ("approve".equalsIgnoreCase(action) ? " approved " : " rejected ") + "successfully";
    }

    public Page<DisplayTalentApprovalDTO> getTalentApprovals(
            String talentName, String agencyName, List<String> statusFilters, int page, int size, String sortBy) {

        logger.info("Getting talent approvals with talentName: {}, agencyName: {}, statusFilters: {}, page: {}, size: {}, sortBy: {}",
                talentName, agencyName, statusFilters, page, size, sortBy);

        // Menentukan default sorting berdasarkan requestDate secara descending
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "requestDate";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, sortBy != null ? sortBy : "requestDate");
        Page<TalentRequest> talentRequestPage;

        if (statusFilters == null || statusFilters.isEmpty()) {
            if (!StringUtils.isEmpty(talentName)) {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCase(talentName, pageable);
            } else if (!StringUtils.isEmpty(agencyName)) {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseOrTalentWishlist_Client_AgencyNameContainingIgnoreCase(talentName, agencyName, pageable);
            } else {
                talentRequestPage = talentRequestRepository.findAll(pageable);
            }
        } else {
            // If status filters are provided, use them in the search
            if (talentName == null || talentName.trim().isEmpty()) {
                talentRequestPage = talentRequestRepository
                        .findByTalentRequestStatus_TalentRequestStatusNameIn(statusFilters, pageable);
            } else {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseAndTalentRequestStatus_TalentRequestStatusNameIn(
                                talentName, statusFilters, pageable);
            }
        }

        return talentRequestPage.map(this::convertToDisplayTalentApprovalDTO);
    }

    private DisplayTalentApprovalDTO convertToDisplayTalentApprovalDTO(TalentRequest talentRequest) {
        DisplayTalentApprovalDTO dto = new DisplayTalentApprovalDTO();
        dto.setTalentRequestId(talentRequest.getTalentRequestId());
        dto.setRequestDate(talentRequest.getRequestDate());

        if (talentRequest.getTalentWishlist() != null &&
                talentRequest.getTalentWishlist().getClient() != null) {
            dto.setAgencyName(talentRequest.getTalentWishlist().getClient().getAgencyName());
        } else {
            dto.setAgencyName(null); // or some default value
        }

        dto.setTalentName(talentRequest.getTalentWishlist().getTalent().getTalentName());
        dto.setApprovalStatus(talentRequest.getTalentRequestStatus().getTalentRequestStatusName());
        return dto;
    }
}
