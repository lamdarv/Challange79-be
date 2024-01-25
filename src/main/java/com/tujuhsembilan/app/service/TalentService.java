package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.dto.TalentDTO;
import com.tujuhsembilan.app.dto.TalentSearchDTO;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.TalentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lib.minio.exception.MinioServiceDownloadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import lib.minio.MinioSrvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class TalentService {
    @Autowired
    private TalentRepository talentRepository;
    @Autowired
    private MinioSrvc minioSrvc;
    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    public Page<TalentDTO> getAllTalents(TalentSearchDTO searchDTO) {
        log.info("Creating Specification...");
        Specification<Talent> spec = createSpecification(searchDTO);

        log.info("Creating Sort...");
        Sort finalSort = createSort(searchDTO);
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), finalSort);

        log.info("Calling talentRepository.findAll...");
        Page<Talent> talentPage = talentRepository.findAll(spec, pageable);
        return talentPage.map(this::convertToDTO);
    }

    private Specification<Talent> createSpecification(TalentSearchDTO searchDTO) {
        log.info("Creating Specification with searchDTO: {}", searchDTO);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDTO.getSearch() != null && !searchDTO.getSearch().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("talentName")), "%" + searchDTO.getSearch().toLowerCase() + "%"));
            }
            if (searchDTO.getTalentLevelName() != null && !searchDTO.getTalentLevelName().trim().isEmpty()) {
                log.info("Adding talent level predicate: {}", searchDTO.getTalentLevelName());
                predicates.add(cb.equal(root.get("talentLevel").get("talentLevelName"), searchDTO.getTalentLevelName()));
            }
            if (searchDTO.getTalentLevelName() != null && !searchDTO.getTalentLevelName().trim().isEmpty()) {
                log.info("Adding talent level predicate: {}", searchDTO.getTalentLevelName());
                predicates.add(cb.equal(root.get("talentLevel").get("talentLevelName"), searchDTO.getTalentLevelName()));
            }
            if (searchDTO.getTalentStatus() != null && !searchDTO.getTalentStatus().trim().isEmpty()) {
                Join<Talent, TalentStatus> talentStatusJoin = root.join("talentStatusId");
                String formattedTalentStatus = searchDTO.getTalentStatus().trim().replaceAll("\\s+", " ");
                predicates.add(cb.equal(cb.lower(talentStatusJoin.get("talentStatus")), formattedTalentStatus.toLowerCase()));
            }
            if (searchDTO.getTagsName() != null && !searchDTO.getTagsName().isEmpty()) {
                Join<Talent, TalentSkillset> talentSkillsetJoin = root.join("talentSkillsets", JoinType.INNER);
                Join<TalentSkillset, Skillset> skillsetJoin = talentSkillsetJoin.join("skillset", JoinType.INNER);

                List<jakarta.persistence.criteria.Predicate> tagPredicates = new ArrayList<>();
                for (String tag : searchDTO.getTagsName()) {
                    tagPredicates.add(cb.equal(cb.lower(skillsetJoin.get("skillsetName")), tag.toLowerCase()));
                }

                predicates.add(cb.or(tagPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }


            if (searchDTO.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), searchDTO.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort createSort(TalentSearchDTO searchDTO) {
        // Log untuk menandakan pembuatan sort dengan searchDTO yang diberikan
        log.info("Creating Sort with searchDTO: {}", searchDTO);

        // Inisialisasi list untuk menyimpan urutan pengurutan
        List<Sort.Order> orders = new ArrayList<>();

        // Mengecek apakah parameter sort disediakan dan tidak kosong
        if (searchDTO.getSort() != null && !searchDTO.getSort().isEmpty()) {
            // Memisahkan parameter sort menjadi array dengan menggunakan ","
            String[] params = searchDTO.getSort().split(",");
            // Memeriksa apakah ada setidaknya dua elemen dalam array params
            if (params.length > 1) {
                // Ekstrak nama field dari elemen pertama array params
                String field = params[0];

                // Memeriksa apakah field terkait dengan TalentLevel
                if ("talentLevelName".equals(field)) {
                    // Jika field adalah talentLevelName, modifikasi field untuk menggunakan path properti yang benar
                    field = "talentLevel.talentLevelName";
                }

                // Memeriksa apakah field terkait dengan talentName
                else if ("talentName".equals(field)) {
                    // Tidak perlu modifikasi field untuk talentName
                    // Dapat menambahkannya ke list orders langsung
                }

                // Menentukan arah pengurutan berdasarkan elemen kedua array params
                String direction = params[1].equalsIgnoreCase("desc") ? "desc" : "asc";

                // Menambahkan urutan pengurutan yang baru ke dalam list orders
                orders.add(new Sort.Order(Sort.Direction.fromString(direction), field));
            }
        }

        // Kembalikan objek Sort yang telah dibuat. Jika list orders kosong, kembalikan Sort yang tidak diurutkan.
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }



//    private Sort createSort(TalentSearchDTO searchDTO) {
//        Sort finalSort = Sort.unsorted();
//
//        if (searchDTO.getTalentExperience() != null && !searchDTO.getTalentExperience().isEmpty()) {
//            String[] talentExperienceParams = searchDTO.getTalentExperience().split(",");
//            Sort talentExperienceSort = Sort.by(talentExperienceParams[0]);
//            if (talentExperienceParams.length > 1) {
//                talentExperienceSort = "asc".equalsIgnoreCase(talentExperienceParams[1]) ? talentExperienceSort.ascending() : talentExperienceSort.descending();
//            }
//            finalSort = talentExperienceSort.and(Sort.by("talentLevel.talentLevelName").descending());
////            finalSort = finalSort.and((talentExperienceSort));
//        }
//
//        if (searchDTO.getTalentName() != null && !searchDTO.getTalentName().isEmpty()) {
//            String[] talentNameParams = searchDTO.getTalentName().split(",");
//            Sort talentNameSort = Sort.by(talentNameParams[0]);
//            talentNameSort = "asc".equalsIgnoreCase(talentNameParams[1]) ? talentNameSort.ascending() : talentNameSort.descending();
//            finalSort = finalSort.and(talentNameSort);
//            System.out.println("Name Sort: " + talentNameSort);
//        }
//
//        // Sort by Talent Level Name
//        if (searchDTO.getTalentLevelName() != null && !searchDTO.getTalentLevelName().isEmpty()) {
//            String[] talentLevelNameParams = searchDTO.getTalentLevelName().split(",");
//            Sort talentLevelNameSort = Sort.by("talentLevel.talentLevelName");
//            talentLevelNameSort = "asc".equalsIgnoreCase(talentLevelNameParams[1]) ? talentLevelNameSort.ascending() : talentLevelNameSort.descending();
//            finalSort = finalSort.and(talentLevelNameSort);
//            System.out.println("Talent Level Name Sort: " + talentLevelNameSort);
//        }
//
//
//        return finalSort;
//    }

    private TalentDTO convertToDTO(Talent talent) {
        TalentDTO dto = new TalentDTO();
        dto.setTalentId(talent.getTalentId());

        try {
            String photoUrl = minioSrvc.getLink("talent-center-app", talent.getTalentPhotoFilename(), TimeUnit.HOURS.toSeconds(1));
            dto.setTalentPhotoUrl(photoUrl);
        } catch (MinioServiceDownloadException e) {
            log.error("Error downloading talent photo for talentId: {}", talent.getTalentId(), e);
            // Handle the exception by setting a default photo URL or providing an error message.
            dto.setTalentPhotoUrl("default-photo-url");
        }


        dto.setTalentName(talent.getTalentName());

        if (talent.getTalentStatus() != null) {
            dto.setTalentStatus(talent.getTalentStatus().getTalentStatusName());
        }

        if (talent.getEmployeeStatus() != null){
            dto.setEmployeeStatus(talent.getEmployeeStatus().getEmployeeStatusName());
        }

        dto.setTalentAvailability(talent.getTalentAvailability());
        dto.setTalentExperience(talent.getTalentExperience());

        if (talent.getTalentLevel() != null) {
            dto.setTalentLevelName(talent.getTalentLevel().getTalentLevelName());
        }

        if (talent.getTalentPositions() != null) {
            List<PositionDTO> positionDTOs = talent.getTalentPositions().stream()
                    .filter(Objects::nonNull)
                    .map(talentPosition -> {
                        Position position = talentPosition.getPosition();
                        if (position != null) {
                            PositionDTO positionDTO = new PositionDTO();
                            positionDTO.setPositionId(position.getPositionId());
                            positionDTO.setPositionName(position.getPositionName());
                            return positionDTO;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            dto.setPositions(positionDTOs);
        }

        if(talent.getTalentSkillsets() != null){
            List<SkillsetDTO> skillsetDTOs = talent.getTalentSkillsets().stream()
                    .filter(Objects::nonNull)
                    .map(talentSkillset -> {
                        Skillset skillset = talentSkillset.getSkillset();
                        if(skillset != null) {
                            return new SkillsetDTO(skillset.getSkillsetId(), skillset.getSkillsetName());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            dto.setSkillsets(skillsetDTOs);
        }

        dto.setIsActive(talent.getIsActive());

        return dto;
    }
}
