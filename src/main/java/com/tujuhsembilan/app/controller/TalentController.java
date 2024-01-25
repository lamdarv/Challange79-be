package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.*;
import com.tujuhsembilan.app.model.Talent;
import com.tujuhsembilan.app.model.TalentMetadata;
import com.tujuhsembilan.app.repository.*;
import com.tujuhsembilan.app.service.DisplayRequestTalentService;
import com.tujuhsembilan.app.service.SaveDataTalentService;
import com.tujuhsembilan.app.service.TalentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lib.minio.MinioSrvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/talent-management")
@CrossOrigin(origins = "http://localhost:3000")
public class TalentController {
    private final TalentRepository talentRepository;
    private final TalentWishlistRepository talentWishlistRepository;
    private final JwtUtils jwtUtils;
    private final TalentMetadataRepository talentMetadataRepository;
    private final DisplayWishlistTalentRepository displayWishlistTalentRepository;
    private final TalentLevelRepository talentLevelRepository;
    private final TalentStatusRepository talentStatusRepository;
    private final EmployeeStatusRepository employeeStatusRepository;

    @Autowired
    private SaveDataTalentService saveDataTalentService;

    @Autowired
    private MinioSrvc minioSrvc;

    @Autowired
    private TalentService talentService;

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    @Autowired
    public TalentController(TalentRepository talentRepository,
                            TalentWishlistRepository talentWishlistRepository,
                            UserRepository userRepository,
                            JwtUtils jwtUtils,
                            TalentMetadataRepository talentMetadataRepository,
                            DisplayWishlistTalentRepository displayWishlistTalentRepository,
                            SaveDataTalentService saveDataTalentService,
                            TalentLevelRepository talentLevelRepository,
                            TalentStatusRepository talentStatusRepository,
                            EmployeeStatusRepository employeeStatusRepository) {
        this.talentRepository = talentRepository;
        this.talentWishlistRepository = talentWishlistRepository;
        this.jwtUtils = jwtUtils;
        this.talentMetadataRepository = talentMetadataRepository;
        this.displayWishlistTalentRepository = displayWishlistTalentRepository;
        this.saveDataTalentService = saveDataTalentService;
        this.talentLevelRepository = talentLevelRepository;
        this.talentStatusRepository = talentStatusRepository;
        this.employeeStatusRepository = employeeStatusRepository;
    }

    //DisplayTalents
    @GetMapping("/talents")
    @Transactional
    public Page<TalentDTO> getAllTalents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        TalentSearchDTO searchDTO = new TalentSearchDTO();
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSort(sort);

        return talentService.getAllTalents(searchDTO);
    }


    //Save Data Talent
    @PostMapping("/talents")
    public ResponseEntity<String> createTalent(
            @RequestParam("talentPhoto") MultipartFile talentPhoto,
            @RequestParam("talentCV") MultipartFile talentCV,
            @RequestParam("talentName") String talentName,
            @RequestParam("talentStatusId") UUID talentStatusId,
            @RequestParam("nip") String nip,
            @RequestParam("sex") String sex,
            @RequestParam("dob") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dob,
            @RequestParam("talentDescription") String talentDescription,
            @RequestParam("talentExperience") Integer talentExperience,
            @RequestParam("talentLevelId") UUID talentLevelId,
            @RequestParam("projectCompleted") Integer projectCompleted,
            @RequestParam("email") String email,
            @RequestParam("cellphone") String cellphone,
            @RequestParam("employeeStatusId") UUID employeeStatusId,
            @RequestParam("videoUrl") String videoURL,
            @RequestParam(required = false) List<UUID> positionIds,
            @RequestParam(required = false) List<UUID> skillsetIds) {

        log.info("Received request to create talent");

        // Create DTO from request parameters
        SaveDataTalentDTO saveDataTalentDTO = new SaveDataTalentDTO();
        saveDataTalentDTO.setTalentName(talentName);
        saveDataTalentDTO.setTalentStatusId(talentStatusId);
        saveDataTalentDTO.setNip(nip);
        saveDataTalentDTO.setSex(sex);
        saveDataTalentDTO.setDob(dob);
        saveDataTalentDTO.setTalentDescription(talentDescription);
        saveDataTalentDTO.setTalentExperience(talentExperience);
        saveDataTalentDTO.setTalentLevelId(talentLevelId);
        saveDataTalentDTO.setProjectCompleted(projectCompleted);
        saveDataTalentDTO.setEmail(email);
        saveDataTalentDTO.setCellphone(cellphone);
        saveDataTalentDTO.setEmployeeStatusId(employeeStatusId);
        saveDataTalentDTO.setVideoUrl(videoURL);

        log.info("DTO created: {}", saveDataTalentDTO);

        try {
            log.info("Received dob: {}", dob);

            saveDataTalentService.createTalent(talentPhoto, talentCV, saveDataTalentDTO, positionIds, skillsetIds);

            log.info("Talent creation service called successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body("Talent successfully created.");
        } catch (Exception e) {
            log.error("Error creating talent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating talent.");
        }
    }


    //API PUT Count Profile
    @PutMapping("/talents/profile-count")
    @Transactional
    public ResponseEntity<String> putCountProfile(@RequestBody ProfileCounterDTO profileCounterDTO){
        try {
            TalentMetadata talentMetadata = talentMetadataRepository.findById(profileCounterDTO.getTalentId())
                    .orElseThrow(() -> new EntityNotFoundException("Talent not found with id: " + profileCounterDTO.getTalentId()));

            talentMetadata.setProfileCounter(talentMetadata.getProfileCounter() + 1);
            talentMetadataRepository.save(talentMetadata);

            return ResponseEntity.ok("Counting profile with id " + talentMetadata.getTalent().getTalentId() + " is " + talentMetadata.getProfileCounter());

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile counter: " + e.getMessage());
        }
    }

    //API POST Download CV
    @PostMapping("/talents/download-cv")
    @Transactional
    public ResponseEntity<?> downloadCV(@RequestBody DownloadCVRequestDTO request, HttpServletResponse response) {
        try {
            Talent talent = talentRepository.findById(request.getTalentId())
                    .orElseThrow(() -> new RuntimeException("Talent not found"));

            String talentCVFilename = talent.getTalentCVFilename();
            if (talentCVFilename == null || talentCVFilename.isEmpty()){
                throw new RuntimeException("CV not available for this talent");
            }

            String bucketName = "talent-center-app";
            minioSrvc.view(response, bucketName, talentCVFilename);
            return ResponseEntity.ok("CV successfully downloaded");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/talents/download-cv-count")
    @Transactional
    public ResponseEntity<?> incrementCVDownloadCount(@RequestBody DownloadCVCountRequestDTO request) {
        try {
            TalentMetadata talentMetadata = talentMetadataRepository.findById(request.getTalentId())
                    .orElseThrow(() -> new RuntimeException("Talent Metadata not found!"));

            talentMetadata.setCvCounter(talentMetadata.getCvCounter() + 1);
            talentMetadataRepository.save(talentMetadata);
            return ResponseEntity.ok("CV Download Count " + talentMetadata.getCvCounter());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}