package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.SaveDataTalentDTO;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.*;
import io.minio.ObjectWriteResponse;
import jakarta.persistence.EntityNotFoundException;
import lib.minio.MinioSrvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SaveDataTalentService {
    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);
    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private SkillsetRepository skillsetRepository;

    @Autowired
    private TalentStatusRepository talentStatusRepository;

    @Autowired
    private TalentLevelRepository talentLevelRepository;

    @Autowired
    private EmployeeStatusRepository employeeStatusRepository;

    @Autowired
    private PositionRepositoryForCreateTalent positionRepositoryForCreateTalent;

    @Autowired
    private SkillsetRepositoryForCreateTalent skillsetRepositoryForCreateTalent;

    @Autowired
    private TalentPositionRepository talentPositionRepository;

    @Autowired
    private TalentSkillsetRepository talentSkillsetRepository;

    @Autowired
    private MinioSrvc minioSrvc;

    public void createTalent(MultipartFile talentPhoto, MultipartFile talentCV, SaveDataTalentDTO saveDataTalentDTO, List<UUID> positionIds, List<UUID> skillsetIds){
        validateFileExtensions(talentPhoto, talentCV);
        String talentLevelName = getTalentLevelNameById(saveDataTalentDTO.getTalentLevelId());

        String talentPhotoFileName = constructFileName(
                saveDataTalentDTO.getTalentName(),
                saveDataTalentDTO.getTalentExperience(),
                talentLevelName,
                talentPhoto.getOriginalFilename()
        );
        String talentCVFileName = constructFileName(
                saveDataTalentDTO.getTalentName(),
                saveDataTalentDTO.getTalentExperience(),
                talentLevelName,
                talentCV.getOriginalFilename());

        // Use MinioSrvc to upload the photo and CV to MinIO
        ObjectWriteResponse photoUploadResponse = minioSrvc.upload(talentPhoto, "talent-center-app",
                o -> MinioSrvc.UploadOption.builder().filename(talentPhotoFileName).build());
        ObjectWriteResponse cvUploadResponse = minioSrvc.upload(talentCV, "talent-center-app",
                o -> MinioSrvc.UploadOption.builder().filename(talentCVFileName).build());

        //Mapping
        Talent talent = mapDtoToEntity(saveDataTalentDTO, talentPhotoFileName, talentCVFileName);
        talent.setIsActive(true);
        talent = talentRepository.save(talent);

        // Create and set the TalentMetadata
        TalentMetadata talentMetadata = TalentMetadata.builder()
                .cvCounter(0)
                .profileCounter(0)
                .build();

        // Set the Talent object in TalentMetadata
        talentMetadata.setTalent(talent);

        // Set the TalentMetadata in Talent
        talent.setTalentMetadata(talentMetadata);

        talentRepository.save(talent);

        savePositionAndSkillset(talent, positionIds, skillsetIds);
    }

    public String getTalentLevelNameById(UUID talentLevelId) {
        // Retrieve talent level name from the repository based on talent level ID
        return talentLevelRepository.findTalentLevelNameByTalentLevelId(talentLevelId)
                .orElse(null);
    }

    private void validateFileExtensions(MultipartFile talentPhoto, MultipartFile talentCV) {
        if (!isValidPhotoExtension(talentPhoto.getOriginalFilename())) {
            throw new IllegalArgumentException("Invalid photo file extension");
        }
        if (!isValidCVExtension(talentCV.getOriginalFilename())) {
            throw new IllegalArgumentException("Invalid CV file extension");
        }
    }

    private boolean isValidPhotoExtension(String filename) {
        String extension = getExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    private boolean isValidCVExtension(String filename) {
        String extension = getExtension(filename).toLowerCase();
        return extension.equals("pdf") || extension.equals("docx");
    }

    private String constructFileName(String talentName, int experience, String talentLevelId, String originalFilename) {
        String timeStamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US)
                .format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        String baseName = talentName.replaceAll("\\s+", "_") + "_" + experience + "_" + talentLevelId + "_" + timeStamp;
        return baseName + getExtension(originalFilename);
    }

    private String getExtension(String originalFilename) {
        int lastIndex = originalFilename.lastIndexOf('.');
        if (lastIndex == -1) {
            return ""; // Empty string when there is no extension
        }
        return originalFilename.substring(lastIndex + 1);
    }

    private Talent mapDtoToEntity(SaveDataTalentDTO saveDataTalentDTO, String photoFileName, String cvFileName) {
        //Talent Level
        UUID talentLevelId = saveDataTalentDTO.getTalentLevelId();
        TalentLevel talentLevel = talentLevelRepository.findById(talentLevelId)
                .orElseThrow(() -> new EntityNotFoundException("Talent Level not found"));

        //Talent Status
        UUID talentStatusId = saveDataTalentDTO.getTalentStatusId();
        TalentStatus talentStatus = talentStatusRepository.findById(talentStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Talent Status not found"));

        //Employee Status
        UUID employeeStatusId = saveDataTalentDTO.getEmployeeStatusId();
        EmployeeStatus employeeStatus = employeeStatusRepository.findById(employeeStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Employee Status not found"));

        Talent talent = new Talent();
        talent.setTalentPhotoFilename(photoFileName);
        talent.setTalentCVFilename(cvFileName);
        talent.setTalentName(saveDataTalentDTO.getTalentName());
        talent.setTalentStatus(talentStatus);
        talent.setTalentLevel(talentLevel);
        talent.setEmployeeNumber(saveDataTalentDTO.getNip());
        talent.setGender(saveDataTalentDTO.getSex());

        log.info("Received dob from DTO: {}", saveDataTalentDTO.getDob());
        LocalDateTime mappedDateTime = saveDataTalentDTO.getDob();
        log.info("Mapped LocalDateTime: {}", mappedDateTime);
        talent.setBirthDate(mappedDateTime);

//        log.info("Received dob from DTO: {}", saveDataTalentDTO.getDob());
//        talent.setBirthDate(saveDataTalentDTO.getDob());
        talent.setTalentDescription(saveDataTalentDTO.getTalentDescription());
        talent.setTalentExperience(saveDataTalentDTO.getTalentExperience());
        talent.setTotalProjectCompleted(saveDataTalentDTO.getProjectCompleted());
        talent.setEmail(saveDataTalentDTO.getEmail());
        talent.setCellphone(saveDataTalentDTO.getCellphone());
        talent.setEmployeeStatus(employeeStatus);
        talent.setBiographyVideoUrl(saveDataTalentDTO.getVideoUrl());
        talent.setAvailability(true);
        talent.setIsActive(true);

        return talent;
    }

    private void savePositionAndSkillset(Talent talent, List<UUID> positionIds, List<UUID> skillsetIds) {
        // Handle Positions
        if (positionIds != null) {
            for (UUID positionId : positionIds) {
                Position position = positionRepositoryForCreateTalent.findById(positionId)
                        .orElseThrow(() -> new EntityNotFoundException("Position not found with id: " + positionId));
                TalentPosition talentPosition = new TalentPosition();
                talentPosition.setTalent(talent);
                talentPosition.setPosition(position);
                talentPositionRepository.save(talentPosition);
            }
        }

        // Handle Skillsets
        if (skillsetIds != null) {
            for (UUID skillsetId : skillsetIds) {
                Skillset skillset = skillsetRepositoryForCreateTalent.findById(skillsetId)
                        .orElseThrow(() -> new EntityNotFoundException("Skillset not found with id: " + skillsetId));
                TalentSkillset talentSkillset = new TalentSkillset();
                talentSkillset.setTalent(talent);
                talentSkillset.setSkillset(skillset);
                talentSkillsetRepository.save(talentSkillset);
            }
        }
    }


}
