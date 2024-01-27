package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.dto.*;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.TalentMetadataRepository;
import com.tujuhsembilan.app.repository.TalentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lib.minio.MinioSrvc;
import lib.minio.exception.MinioServiceDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TalentMetadataRepository talentMetadataRepository;
    @Autowired
    private MinioSrvc minioSrvc;
    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    // Display Talents
    public Page<TalentDTO> getAllTalents(TalentSearchDTO searchDTO) {
        log.info("Creating Specification...");

        // Membuat objek Specification<Talent> berdasarkan kriteria pencarian dari objek TalentSearchDTO
        Specification<Talent> spec = createSpecification(searchDTO);

        log.info("Creating Sort...");

        // Membuat objek Sort berdasarkan kriteria pengurutan dari objek TalentSearchDTO
        Sort finalSort = createSort(searchDTO);
        // Membuat objek Pageable berdasarkan kriteria halaman, ukuran halaman, dan objek Sort yang sudah dibuat
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), finalSort);

        log.info("Calling talentRepository.findAll...");

        // Memanggil metode findAll dari talentRepository dengan menggunakan Specification dan Pageable yang sudah dibuat
        Page<Talent> talentPage = talentRepository.findAll(spec, pageable);

        // Mengonversi setiap objek Talent dalam halaman menjadi objek TalentDTO dan mengembalikan halaman yang sudah diubah
        return talentPage.map(this::convertToDTO);
    }

    // Metode untuk membuat objek Specification<Talent> berdasarkan objek TalentSearchDTO
    private Specification<Talent> createSpecification(TalentSearchDTO searchDTO) {
        log.info("Creating Specification with searchDTO: {}", searchDTO);

        // Mengembalikan objek Specification menggunakan ekspresi lambda
        return (root, query, cb) -> {
            // Membuat list untuk menampung semua kondisi (predikat)
            List<Predicate> predicates = new ArrayList<>();

            //Menambahkan kondisi bahwa nama talent harus mengandung kata kunci dari searchDTO
            if (searchDTO.getSearch() != null && !searchDTO.getSearch().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("talentName")), "%" + searchDTO.getSearch().toLowerCase() + "%"));
            }

            // Menambahkan kondisi bahwa nama level talent harus sama dengan nilai dari searchDTO
            if (searchDTO.getTalentLevelName() != null && !searchDTO.getTalentLevelName().trim().isEmpty()) {
                log.info("Adding talent level predicate: {}", searchDTO.getTalentLevelName());
                predicates.add(cb.equal(root.get("talentLevel").get("talentLevelName"), searchDTO.getTalentLevelName()));
            }

            // Menambahkan kondisi bahwa status talent harus sama dengan nilai dari searchDTO
            if (searchDTO.getTalentStatus() != null && !searchDTO.getTalentStatus().trim().isEmpty()) {
                // Membuat join terpisah untuk status talent
                Join<Talent, TalentStatus> talentStatusJoin = root.join("talentStatusId");
                // Format ulang nilai talentStatus dari searchDTO untuk menghindari masalah whitespace
                String formattedTalentStatus = searchDTO.getTalentStatus().trim().replaceAll("\\s+", " ");
                // Menambahkan kondisi bahwa nama talentStatus harus cocok dengan nilai formattedTalentStatus, tanpa memperhatikan huruf besar atau kecil
                predicates.add(cb.equal(cb.lower(talentStatusJoin.get("talentStatus")), formattedTalentStatus.toLowerCase()));
            }

            // Menambahkan kondisi untuk pencarian berdasarkan tags (skillsets)
            if (searchDTO.getTagsName() != null && !searchDTO.getTagsName().isEmpty()) {
                // Memastikan tidak adanya duplikasi
                query.distinct(true);

                // Membuat list untuk menampung semua predikat tag
                List<Predicate> tagPredicates = new ArrayList<>();

                // Menerapkan kondisi 'AND' di setiap tag
                for (String tag : searchDTO.getTagsName()) {
                    // Membuat join terpisah untuk setiap tag di dalam loop
                    Join<Talent, TalentSkillset> talentSkillsetJoin = root.join("talentSkillsets", JoinType.INNER);
                    Join<TalentSkillset, Skillset> skillsetJoin = talentSkillsetJoin.join("skillset", JoinType.INNER);

                    // Menambahkan kondisi bahwa nama skillset harus cocok dengan tag, tanpa memperhatikan huruf besar atau kecil
                    tagPredicates.add(cb.equal(cb.lower(skillsetJoin.get("skillsetName")), tag.toLowerCase()));
                }

                // Melakukan kombinasi semua predikat tags menggunakan 'AND'
                Predicate allTagConditions = cb.conjunction();
                for (Predicate tagPredicate : tagPredicates) {
                    allTagConditions = cb.and(allTagConditions, tagPredicate);
                }

                // Menambahkan kondisi 'AND' yang digabungkan untuk tag ke dalam list utama predicates
                predicates.add(allTagConditions);
            }

            // Memeriksa apakah nilai isActive dari objek searchDTO tidak null
            if (searchDTO.getIsActive() != null) {
                // Menambahkan kondisi ke dalam list predicates bahwa nilai isActive dari root harus sama dengan nilai isActive dari searchDTO
                predicates.add(cb.equal(root.get("isActive"), searchDTO.getIsActive()));
            }

            // Mengembalikan kondisi gabungan menggunakan operator AND dari list predicates
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Metode untuk membuat objek Sort berdasarkan objek TalentSearchDTO
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

                //Tidak perlu memeriksa apakah field terkait dengan talentName karena sudah bisa menambahkannya ke list order langsung

                // Menentukan arah pengurutan berdasarkan elemen kedua array params
                String direction = params[1].equalsIgnoreCase("desc") ? "desc" : "asc";

                // Menambahkan urutan pengurutan yang baru ke dalam list orders
                orders.add(new Sort.Order(Sort.Direction.fromString(direction), field));
            }
        }

        // Kembalikan objek Sort yang telah dibuat. Jika list orders kosong, kembalikan Sort yang tidak diurutkan.
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    // Metode ini mengonversi objek Talent ke objek TalentDTO
    private TalentDTO convertToDTO(Talent talent) {
        // Membuat objek baru dari kelas TalentDTO
        TalentDTO dto = new TalentDTO();
        // Mengatur nilai TalentId dari objek Talent ke objek TalentDTO
        dto.setTalentId(talent.getTalentId());

        try {
            // Mengambil URL foto dari Minio menggunakan nama file foto dan menyimpannya di objek TalentDTO
            String photoUrl = minioSrvc.getLink("talent-center-app", talent.getTalentPhotoFilename(), TimeUnit.HOURS.toSeconds(1));
            dto.setTalentPhotoUrl(photoUrl);
        } catch (MinioServiceDownloadException e) {
            // Menangani exception jika terjadi kesalahan saat mengunduh foto dari Minio
            log.error("Error downloading talent photo for talentId: {}", talent.getTalentId(), e);
            // Menetapkan URL foto default atau memberikan pesan kesalahan jika terjadi exception
            dto.setTalentPhotoUrl("default-photo-url");
        }

        // Mengatur nilai TalentName dari objek Talent ke objek TalentDTO
        dto.setTalentName(talent.getTalentName());

        // Mengatur nilai TalentStatus dari objek Talent ke objek TalentDTO jika tidak null
        if (talent.getTalentStatus() != null) {
            dto.setTalentStatus(talent.getTalentStatus().getTalentStatusName());
        }

        // Mengatur nilai EmployeeStatus dari objek Talent ke objek TalentDTO jika tidak null
        if (talent.getEmployeeStatus() != null){
            dto.setEmployeeStatus(talent.getEmployeeStatus().getEmployeeStatusName());
        }

        // Mengatur nilai TalentAvailability dari objek Talent ke objek TalentDTO
        dto.setTalentAvailability(talent.getTalentAvailability());
        // Mengatur nilai TalentExperience dari objek Talent ke objek TalentDTO
        dto.setTalentExperience(talent.getTalentExperience());

        // Mengatur nilai TalentLevelName dari objek Talent ke objek TalentDTO jika tidak null
        if (talent.getTalentLevel() != null) {
            dto.setTalentLevelName(talent.getTalentLevel().getTalentLevelName());
        }

        // Mengatur nilai Positions dari objek Talent ke objek TalentDTO dengan mengonversi objek Position ke objek PositionDTO
        if (talent.getTalentPositions() != null) {
            // Menggunakan stream untuk memproses setiap elemen dalam daftar TalentPosition
            List<PositionDTO> positionDTOs = talent.getTalentPositions().stream()
                    // Menggunakan filter untuk menghapus elemen yang null dari stream
                    .filter(Objects::nonNull)
                    // Menggunakan map untuk mengonversi setiap TalentPosition menjadi PositionDTO
                    .map(talentPosition -> {
                        // Mendapatkan objek Position dari TalentPosition
                        Position position = talentPosition.getPosition();
                        // Memeriksa apakah objek Position tidak null
                        if (position != null) {
                            // Membuat objek baru dari kelas PositionDTO
                            PositionDTO positionDTO = new PositionDTO();
                            // Mengatur nilai PositionId dari objek Position ke objek PositionDTO
                            positionDTO.setPositionId(position.getPositionId());
                            // Mengatur nilai PositionName dari objek Position ke objek PositionDTO
                            positionDTO.setPositionName(position.getPositionName());
                            // Mengembalikan objek PositionDTO yang sudah dibuat
                            return positionDTO;
                        }
                        // Jika objek Position null, mengembalikan null
                        return null;
                    })
                    // Menggunakan filter lagi untuk menghapus elemen yang null dari stream hasil map
                    .filter(Objects::nonNull)
                    // Mengumpulkan hasil stream menjadi List<PositionDTO>
                    .collect(Collectors.toList());

            // Mengatur nilai Positions dari objek TalentDTO dengan List<PositionDTO> yang sudah dibuat
            dto.setPositions(positionDTOs);
        }

        // Mengatur nilai Skillsets dari objek Talent ke objek TalentDTO dengan mengonversi objek Skillset ke objek SkillsetDTO
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

        // Mengatur nilai IsActive dari objek Talent ke objek TalentDTO
        dto.setIsActive(talent.getIsActive());

        // Mengembalikan objek TalentDTO yang sudah dikonversi
        return dto;
    }

    // PUT Count Profile
    @Transactional
    public String updateProfileCounter(ProfileCounterDTO profileCounterDTO) {
        try {
            // Mencari objek TalentMetadata berdasarkan talentId yang diberikan
            TalentMetadata talentMetadata = talentMetadataRepository.findById(profileCounterDTO.getTalentId())
                    // Mengembalikan EntityNotFoundException jika tidak ditemukan
                    .orElseThrow(() -> new EntityNotFoundException("Talent not found with id: " + profileCounterDTO.getTalentId()));

            // Menambahkan satu ke jumlah profil pada objek TalentMetadata
            talentMetadata.setProfileCounter(talentMetadata.getProfileCounter() + 1);
            // Menyimpan perubahan ke dalam repositori
            talentMetadataRepository.save(talentMetadata);

            // Mengembalikan pesan sukses berisi informasi tentang profil yang dihitung
            return "Counting profile with id " + talentMetadata.getTalent().getTalentId() + " is " + talentMetadata.getProfileCounter();
        } catch (EntityNotFoundException e) {
            // Melemparkan kembali EntityNotFoundException untuk ditangani di dalam controller
            throw e;
        } catch (Exception e) {
            // Melemparkan RuntimeException jika terjadi kesalahan yang tidak diharapkan
            throw new RuntimeException("Error updating profile counter: " + e.getMessage(), e);
        }
    }

    //POST Download CV
    @Transactional
    public void downloadCV(DownloadCVRequestDTO request, HttpServletResponse response) {
        try {
            // Mencari objek Talent berdasarkan talentId yang diberikan
            Talent talent = talentRepository.findById(request.getTalentId())
                    // Melemparkan RuntimeException jika tidak ditemukan
                    .orElseThrow(() -> new RuntimeException("Talent not found"));

            // Mendapatkan nama file CV dari objek Talent
            String talentCVFilename = talent.getTalentCVFilename();

            // Melemparkan RuntimeException jika nama file CV tidak tersedia atau kosong
            if (talentCVFilename == null || talentCVFilename.isEmpty()) {
                throw new RuntimeException("CV not available for this talent");
            }

            // Nama bucket untuk penyimpanan Minio
            String bucketName = "talent-center-app";

            // Memanggil metode view dari MinioService untuk menampilkan file CV ke dalam HttpServletResponse
            minioSrvc.view(response, bucketName, talentCVFilename);
        } catch (Exception e) {
            // Throw RuntimeException jika terjadi kesalahan selama proses pengunduhan CV
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Transactional
    public String incrementCVDownloadCount(DownloadCVCountRequestDTO request) {
        try {
            // Mencari objek TalentMetadata berdasarkan talentId yang diberikan
            TalentMetadata talentMetadata = talentMetadataRepository.findById(request.getTalentId())
                    // Melemparkan RuntimeException jika tidak ditemukan
                    .orElseThrow(() -> new RuntimeException("Talent Metadata not found!"));

            // Menambah satu ke nilai cvCounter pada objek TalentMetadata
            talentMetadata.setCvCounter(talentMetadata.getCvCounter() + 1);

            // Menyimpan perubahan ke dalam repositori
            talentMetadataRepository.save(talentMetadata);

            // Mengembalikan pesan sukses berisi informasi tentang jumlah unduhan CV yang sudah ditingkatkan
            return "CV Download Count " + talentMetadata.getCvCounter();
        } catch (Exception e) {
            // Throw RuntimeException jika terjadi kesalahan selama proses peningkatan jumlah unduhan CV
            throw new RuntimeException(e.getMessage(), e);
        }
    }



}
