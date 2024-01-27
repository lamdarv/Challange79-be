package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.DisplayTalentApprovalDTO;
import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentRequestStatus;
import com.tujuhsembilan.app.repository.TalentRequestRepository;
import com.tujuhsembilan.app.repository.TalentRequestStatusRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class TalentApprovalService {
    private final TalentRequestRepository talentRequestRepository;

    private final TalentRequestStatusRepository talentRequestStatusRepository;
    private static final Logger logger = LoggerFactory.getLogger(TalentApprovalService.class);

    @Autowired
    public TalentApprovalService(
            TalentRequestRepository talentRequestRepository,
            TalentRequestStatusRepository talentRequestStatusRepository
    ){
        this.talentRequestRepository = talentRequestRepository;
        this.talentRequestStatusRepository = talentRequestStatusRepository;
    }

    @Transactional
    public String approveOrRejectTalentRequest(UUID talentRequestId, String action, String rejectReason) {
        // Mendapatkan talentRequest dari repository berdasarkan talentRequestId
        TalentRequest talentRequest = talentRequestRepository.findById(talentRequestId)
                // Throw IllegalStateException jika tidak ditemukan
                .orElseThrow(() -> new IllegalStateException("Talent request not found"));

        // Mendapatkan TalentRequestStatus baru berdasarkan tindakan (approve/reject)
        TalentRequestStatus newStatus = talentRequestStatusRepository.findByTalentRequestStatusName(
                        "approve".equalsIgnoreCase(action) ? "Approved" : "Rejected")
                // Throw IllegalStateException jika tidak ditemukan.
                .orElseThrow(() -> new IllegalStateException("Status not found"));

        // Jika actionnya reject dan rejectReason null, throw IllegalArgumentException.
        if ("reject".equalsIgnoreCase(action) && (rejectReason == null || rejectReason.isEmpty())) {
            throw new IllegalArgumentException("Reject reason must be provided");
        }

        // Mengubah status talentRequest dan mengatur LastModifiedTime
        talentRequest.setTalentRequestStatus(newStatus);
        talentRequest.setLastModifiedTime(LocalDateTime.now());

        // Jika actionnya reject, set reject reason pada talent request
        if ("reject".equalsIgnoreCase(action)) {
            talentRequest.setRequestRejectReason(rejectReason);
        }

        // Menyimpan perubahan pada talentRequest ke dalam repository.
        talentRequestRepository.save(talentRequest);

        // Mengembalikan pesan berhasil berdasarkan action yang dilakukan.
        return "Talent request with id " + talentRequest.getTalentRequestId() + ("approve".equalsIgnoreCase(action) ? " approved " : " rejected ") + "successfully";
    }

    @Transactional
    public Page<DisplayTalentApprovalDTO> getTalentApprovals(
            String talentName, String agencyName, List<String> statusFilters, int page, int size, String sortBy) {

        // Log informasi mengenai parameter yang diterima
        logger.info("Getting talent approvals with talentName: {}, agencyName: {}, statusFilters: {}, page: {}, size: {}, sortBy: {}",
                talentName, agencyName, statusFilters, page, size, sortBy);

        // Menentukan default sorting berdasarkan requestDate secara descending (default) jika sortBy tidak diberikan
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "requestDate";
        }

        // Membuat objek Pageable untuk digunakan dalam query database
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, sortBy);
        Page<TalentRequest> talentRequestPage;

        // Memeriksa apakah statusFilters tidak diberikan atau kosong
        if (statusFilters == null || statusFilters.isEmpty()) {
            // Jika talentName diberikan, mencari berdasarkan talentName
            if (!StringUtils.isEmpty(talentName)) {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCase(talentName, pageable);
            }
            // Jika agencyName diberikan, mencari berdasarkan talentName atau agencyName
            else if (!StringUtils.isEmpty(agencyName)) {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseOrTalentWishlist_Client_AgencyNameContainingIgnoreCase(talentName, agencyName, pageable);
            }
            // Jika tidak ada talentName atau agencyName, ambil semua datanya
            else {
                talentRequestPage = talentRequestRepository.findAll(pageable);
            }
        } else {
            // Jika statusFilters diberikan, gunakan dalam pencarian
            if (talentName == null || talentName.trim().isEmpty()) {
                talentRequestPage = talentRequestRepository
                        .findByTalentRequestStatus_TalentRequestStatusNameIn(statusFilters, pageable);
            }
            // Jika talentName diberikan, mencari berdasarkan talentName dan statusFilters
            else {
                talentRequestPage = talentRequestRepository
                        .findByTalentWishlist_Talent_TalentNameContainingIgnoreCaseAndTalentRequestStatus_TalentRequestStatusNameIn(
                                talentName, statusFilters, pageable);
            }
        }

        // Mengubah Page<TalentRequest> menjadi Page<DisplayTalentApprovalDTO> menggunakan metode map
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
