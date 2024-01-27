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
        // Validasi ekstensi file untuk foto dan CV
        validateFileExtensions(talentPhoto, talentCV);

        // Mendapatkan talentLevelName berdasarkan ID yang diberikan
        String talentLevelName = getTalentLevelNameById(saveDataTalentDTO.getTalentLevelId());

        // Membuat nama file untuk foto dan CV berdasarkan informasi talent
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

        // Menggunakan MinioSrvc untuk mengunggah foto dan CV ke MinIO
        ObjectWriteResponse photoUploadResponse = minioSrvc.upload(talentPhoto, "talent-center-app",
                o -> MinioSrvc.UploadOption.builder().filename(talentPhotoFileName).build());
        ObjectWriteResponse cvUploadResponse = minioSrvc.upload(talentCV, "talent-center-app",
                o -> MinioSrvc.UploadOption.builder().filename(talentCVFileName).build());

        // Pemetaan (Mapping) dari SaveDataTalentDTO ke objek Talent
        Talent talent = mapDtoToEntity(saveDataTalentDTO, talentPhotoFileName, talentCVFileName);
        talent.setIsActive(true);

        // Menyimpan objek Talent ke dalam repositori
        talent = talentRepository.save(talent);

        // Membuat dan menetapkan objek TalentMetadata
        TalentMetadata talentMetadata = TalentMetadata.builder()
                .cvCounter(0)
                .profileCounter(0)
                .build();

        // Menetapkan objek TalentMetadata pada Talent
        talent.setTalentMetadata(talentMetadata);

        // Menyimpan perubahan ke dalam repositori
        talentRepository.save(talent);

        // Menyimpan posisi dan keterampilan bakat ke dalam repositori
        savePositionAndSkillset(talent, positionIds, skillsetIds);
    }

    // Metode untuk mendapatkan talentLevelName berdasarkan ID
    public String getTalentLevelNameById(UUID talentLevelId) {
        // Mengambil talentLevelName dari repositori berdasarkan ID level bakat
        return talentLevelRepository.findTalentLevelNameByTalentLevelId(talentLevelId)
                // Mengembalikan null jika tidak ada talentLevelName yang ditemukan
                .orElse(null);
    }


    // Metode untuk memvalidasi ekstensi file untuk foto dan CV
    private void validateFileExtensions(MultipartFile talentPhoto, MultipartFile talentCV) {
        // Memeriksa apakah ekstensi file foto valid
        if (!isValidPhotoExtension(talentPhoto.getOriginalFilename())) {
            // Throw IllegalArgumentException jika ekstensi file foto tidak valid
            throw new IllegalArgumentException("Invalid photo file extension");
        }

        // Memeriksa apakah ekstensi file CV valid
        if (!isValidCVExtension(talentCV.getOriginalFilename())) {
            // Throw IllegalArgumentException jika ekstensi file CV tidak valid
            throw new IllegalArgumentException("Invalid CV file extension");
        }
    }


    // Metode untuk memeriksa apakah ekstensi file foto valid
    private boolean isValidPhotoExtension(String filename) {
        // Mendapatkan ekstensi file dan mengonversinya menjadi huruf kecil (lowercase)
        String extension = getExtension(filename).toLowerCase();

        // Memeriksa apakah ekstensi file sesuai dengan format yang diperbolehkan (jpg, jpeg, atau png)
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }


    // Metode untuk memvalidasi ekstensi file CV yang diperbolehkan
    private boolean isValidCVExtension(String filename) {
        // Mendapatkan ekstensi file dari nama file dan mengonversikannya ke huruf kecil
        String extension = getExtension(filename).toLowerCase();

        // Memeriksa apakah ekstensi file adalah "pdf" atau "docx"
        return extension.equals("pdf") || extension.equals("docx");
    }


    // Metode untuk membuat nama file berdasarkan informasi talent dan nama asli file
    private String constructFileName(String talentName, int experience, String talentLevelId, String originalFilename) {
        // Mendapatkan timestamp dalam format tertentu menggunakan LocalDateTime dan Instant
        String timeStamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US)
                .format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        // Membuat nama dasar file dengan menggabungkan informasi talent, yaitu pengalaman, ID level bakat, dan timestamp
        String baseName = talentName.replaceAll("\\s+", "_") + "_" + experience + "_" + talentLevelId + "_" + timeStamp;

        // Mengembalikan nama file lengkap dengan menambahkan ekstensi dari nama asli file
        return baseName + getExtension(originalFilename);
    }


    // Metode untuk mendapatkan ekstensi file dari nama file asli
    private String getExtension(String originalFilename) {
        // Mencari indeks terakhir dari karakter '.' dalam nama file
        int lastIndex = originalFilename.lastIndexOf('.');

        // Mengembalikan string kosong jika tidak ada ekstensi
        if (lastIndex == -1) {
            return "";
        }

        // Mengambil substring setelah karakter '.' untuk mendapatkan ekstensi
        return originalFilename.substring(lastIndex + 1);
    }


    // Metode untuk memetakan objek DTO (SaveDataTalentDTO) ke objek Talent
    private Talent mapDtoToEntity(SaveDataTalentDTO saveDataTalentDTO, String photoFileName, String cvFileName) {
        // Mendapatkan objek TalentLevel berdasarkan ID yang diberikan
        UUID talentLevelId = saveDataTalentDTO.getTalentLevelId();
        TalentLevel talentLevel = talentLevelRepository.findById(talentLevelId)
                // Throw EntityNotFoundException jika tidak ditemukan
                .orElseThrow(() -> new EntityNotFoundException("Talent Level not found"));

        // Mendapatkan objek TalentStatus berdasarkan ID yang diberikan
        UUID talentStatusId = saveDataTalentDTO.getTalentStatusId();
        TalentStatus talentStatus = talentStatusRepository.findById(talentStatusId)
                // Throw EntityNotFoundException jika tidak ditemukan
                .orElseThrow(() -> new EntityNotFoundException("Talent Status not found"));

        // Mendapatkan objek EmployeeStatus berdasarkan ID yang diberikan
        UUID employeeStatusId = saveDataTalentDTO.getEmployeeStatusId();
        EmployeeStatus employeeStatus = employeeStatusRepository.findById(employeeStatusId)
                // Throw EntityNotFoundException jika tidak ditemukan
                .orElseThrow(() -> new EntityNotFoundException("Employee Status not found"));

        // Membuat objek Talent dan menetapkan nilai atribut menggunakan data dari DTO
        Talent talent = new Talent();
        talent.setTalentPhotoFilename(photoFileName);
        talent.setTalentCVFilename(cvFileName);
        talent.setTalentName(saveDataTalentDTO.getTalentName());
        talent.setTalentStatus(talentStatus);
        talent.setTalentLevel(talentLevel);
        talent.setEmployeeNumber(saveDataTalentDTO.getNip());
        talent.setGender(saveDataTalentDTO.getSex());

        log.info("Received dob from DTO: {}", saveDataTalentDTO.getDob());

        // Mendapatkan nilai LocalDateTime dari DTO
        LocalDateTime mappedDateTime = saveDataTalentDTO.getDob();

        log.info("Mapped LocalDateTime: {}", mappedDateTime);

        talent.setBirthDate(mappedDateTime);
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

    // Metode untuk menyimpan position dan skillset ke dalam repositori
    private void savePositionAndSkillset(Talent talent, List<UUID> positionIds, List<UUID> skillsetIds) {
        // Handle Positions
        if (positionIds != null) {
            // Iterasi melalui setiap ID posisi
            for (UUID positionId : positionIds) {
                // Mencari objek Position berdasarkan ID posisi
                Position position = positionRepositoryForCreateTalent.findById(positionId)
                        // Melemparkan EntityNotFoundException jika tidak ditemukan
                        .orElseThrow(() -> new EntityNotFoundException("Position not found with id: " + positionId));
                // Membuat objek TalentPosition dan menetapkan objek Talent dan Position
                TalentPosition talentPosition = new TalentPosition();
                talentPosition.setTalent(talent);
                talentPosition.setPosition(position);
                // Menyimpan objek TalentPosition ke dalam repositori
                talentPositionRepository.save(talentPosition);
            }
        }

        // Handle Skillsets
        if (skillsetIds != null) {
            // Iterasi melalui setiap ID Skillsets
            for (UUID skillsetId : skillsetIds) {
                // Mencari objek Skillset berdasarkan ID Skillsets
                Skillset skillset = skillsetRepositoryForCreateTalent.findById(skillsetId)
                        // Throw EntityNotFoundException jika tidak ditemukan
                        .orElseThrow(() -> new EntityNotFoundException("Skillset not found with id: " + skillsetId));

                // Membuat objek TalentSkillset dan menetapkan objek Talent dan Skillset
                TalentSkillset talentSkillset = new TalentSkillset();
                talentSkillset.setTalent(talent);
                talentSkillset.setSkillset(skillset);

                // Menyimpan objek TalentSkillset ke dalam repositori
                talentSkillsetRepository.save(talentSkillset);
            }
        }
    }
}
