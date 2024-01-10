package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.DisplayTalentApprovalDTO;
import com.tujuhsembilan.app.dto.TalentApprovalDTO;
import com.tujuhsembilan.app.model.TalentRequest;
import com.tujuhsembilan.app.model.TalentRequestStatus;
import com.tujuhsembilan.app.repository.TalentRequestRepository;
import com.tujuhsembilan.app.repository.TalentRequestStatusRepository;
import com.tujuhsembilan.app.service.TalentApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/talent-management")
public class TalentApprovalController {
    @Autowired
    private TalentApprovalService talentApprovalService;

    //API PUT Approve/Reject Persetujuan Talent
    @PutMapping("/talent-approvals")
    public ResponseEntity<String> talentApprove(@RequestBody TalentApprovalDTO talentApprovalDTO) {
        try {
            if (talentApprovalDTO.getTalentRequestId() == null || talentApprovalDTO.getAction() == null) {
                return new ResponseEntity<>("Invalid request parameters", HttpStatus.BAD_REQUEST);
            }

            String message = talentApprovalService.approveOrRejectTalentRequest(
                    talentApprovalDTO.getTalentRequestId(),
                    talentApprovalDTO.getAction(),
                    talentApprovalDTO.getRejectReason());

            return new ResponseEntity<>(message, HttpStatus.OK);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/talent-approvals")
    @Transactional
    public ResponseEntity<Page<DisplayTalentApprovalDTO>> getTalentApprovals(
            @RequestParam(required = false) String talentName,
            @RequestParam(required = false) String agencyName,
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {

        Page<DisplayTalentApprovalDTO> displayTalentApprovalDTOs = talentApprovalService.getTalentApprovals(
                talentName, agencyName, status, page, size, sortBy);
        return ResponseEntity.ok(displayTalentApprovalDTOs);
    }


//    public ResponseEntity<String> approveTalentRequest(@RequestBody TalentApprovalDTO talentApprovalDTO) {
//        try {
//            talentApprovalService.approveTalentRequest(talentApprovalDTO.getTalentRequestId());
//            return ResponseEntity.ok(talentApprovalDTO.getTalentRequestId() + " successfully approved!");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error approving request with id " + talentApprovalDTO.getTalentRequestId());
//        }
//    }

}
