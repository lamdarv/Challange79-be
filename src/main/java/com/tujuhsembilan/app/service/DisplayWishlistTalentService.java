package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.controller.TalentWishlistController;
import com.tujuhsembilan.app.dto.DisplayWishlistTalentDTO;
import com.tujuhsembilan.app.dto.PositionDTO;
import com.tujuhsembilan.app.dto.SkillsetDTO;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.DisplayWishlistTalentRepository;
import com.tujuhsembilan.app.repository.TalentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisplayWishlistTalentService {
    private final DisplayWishlistTalentRepository displayWishlistTalentRepository;
    private final TalentRepository talentRepository;

    @Autowired
    public DisplayWishlistTalentService(
            DisplayWishlistTalentRepository displayWishlistTalentRepository,
            TalentRepository talentRepository
    ){
        this.displayWishlistTalentRepository = displayWishlistTalentRepository;
        this.talentRepository = talentRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(TalentWishlistController.class);

    @Transactional
    public ResponseEntity<Page<DisplayWishlistTalentDTO>> getWishlistTalentsByUser(User user, int page, int size) {
        // Mendapatkan objek client dari objek user
        Client client = user.getClient();

        // Memeriksa apakah objek klien tidak null, menunjukkan bahwa ada klien terkait dengan pengguna.
        if (client != null) {
            // Mendapatkan ID client dari objek klien.
            UUID clientId = client.getClientId();

            // Menampilkan Client Id pada Log
            log.info("Client ID: {}", clientId);

            // Membuat objek Pageable menggunakan informasi halaman dan ukuran halaman
            Pageable pageable = PageRequest.of(page, size);

            // Memanggil metode getAllWishlistTalentsByClientId dengan menyertakan ID client, flag boolean, dan objek Pageable
            // Hasilnya adalah halaman objek DTO yang merupakan wishlist talent
            Page<DisplayWishlistTalentDTO> result = getAllWishlistTalentsByClientId(clientId, true, pageable);

            // Mengembalikan respons HTTP 200 OK bersama dengan halaman objek DTO yang berisi wishlist talent
            return ResponseEntity.ok(result);
        } else {
            // Menampilkan ke log bahwa tidak ada client yang terkait dengan User Id yang diberikan
            log.warn("No client associated with user ID: {}", user.getUserId());

            // Mengembalikan respons HTTP 404 Not Found karena tidak ada client yang terkait dengan user
            return ResponseEntity.notFound().build();
        }
    }

    public Page<DisplayWishlistTalentDTO> getAllWishlistTalentsByClientId(UUID clientId, boolean isActive, Pageable pageable) {
        // Memanggil repository untuk mendapatkan halaman objek TalentWishlist berdasarkan ID client dan status aktif menggunakan informasi halaman
        Page<TalentWishlist> talentWishlists = displayWishlistTalentRepository.findByClient_ClientIdAndIsActive(clientId, isActive, pageable);

        // Mengonversi halaman TalentWishlist menjadi daftar objek DTO (DisplayWishlistTalentDTO).
        List<DisplayWishlistTalentDTO> sortedList = talentWishlists.getContent()
                .stream()
                .map(this::mapToDTO) // Menggunakan metode mapToDTO untuk mengonversi setiap TalentWishlist menjadi DisplayWishlistTalentDTO.
                .sorted(Comparator.comparing(dto -> dto.getTalentLevel())) // Mengurutkan daftar objek DTO berdasarkan telentLevel
                .collect(Collectors.toList());

        // Membungkus objek list DTO yang telah diurutkan ke dalam objek PageImpl untuk dikembalikkan sebagai hasilnya
        return new PageImpl<>(sortedList, pageable, talentWishlists.getTotalElements());
    }

    // Metode untuk mengonversi objek TalentWishlist menjadi objek DTO (DisplayWishlistTalentDTO).
    private DisplayWishlistTalentDTO mapToDTO(TalentWishlist talentWishlist) {
        // Membuat objek DTO baru.
        DisplayWishlistTalentDTO dto = new DisplayWishlistTalentDTO();

        // Menetapkan nilai atribut DTO berdasarkan nilai dari objek TalentWishlist.
        dto.setWishlistId(talentWishlist.getTalentWishlistId());
        dto.setTalentId(talentWishlist.getTalent().getTalentId());

        // Mengambil objek Talent berdasarkan ID talent dari repository.
        Talent talent = talentRepository.findById(talentWishlist.getTalent().getTalentId()).orElse(null);

        // Memeriksa apakah objek Talent ditemukan.
        if (talent != null) {
            // Mengisi atribut DTO dengan informasi dari objek Talent.
            dto.setTalentName(talent.getTalentName());
            dto.setTalentAvailability(talent.getTalentAvailability());
            dto.setTalentExperience(talent.getTalentExperience());
            dto.setTalentLevel(talent.getTalentLevel().getTalentLevelName());

            // Mengonversi daftar posisi talent menjadi daftar objek DTO (PositionDTO).
            List<PositionDTO> positionDTOs = mapPositions(talent.getTalentPositions());
            dto.setPositions(positionDTOs);

            // Mengonversi daftar skillset talent menjadi daftar objek DTO (SkillsetDTO).
            List<SkillsetDTO> skillsetDTOs = mapSkillsets(talent.getTalentSkillsets());
            dto.setSkillsets(skillsetDTOs);
        }

        // Mengembalikan objek DTO yang telah diisi.
        return dto;
    }

    // Metode untuk mengonversi daftar objek TalentPosition menjadi daftar objek DTO (PositionDTO).
    private List<PositionDTO> mapPositions(List<TalentPosition> talentPositions) {
        // Membuat list array baru untuk menyimpan objek DTO hasil konversi.
        List<PositionDTO> positionDTOs = new ArrayList<>();

        // Iterasi melalui setiap objek TalentPosition dalam list talentPositions.
        for (TalentPosition talentPosition : talentPositions) {
            // Memeriksa apakah objek TalentPosition dan objek Position tidak null.
            if (talentPosition != null && talentPosition.getPosition() != null) {
                // Membuat objek DTO baru dan menetapkan nilai atribut berdasarkan objek Position.
                PositionDTO positionDTO = new PositionDTO();
                positionDTO.setPositionId(talentPosition.getPosition().getPositionId());
                positionDTO.setPositionName(talentPosition.getPosition().getPositionName());

                // Menambahkan objek DTO ke daftar hasil konversi.
                positionDTOs.add(positionDTO);
            }
        }

        // Mengembalikan daftar objek DTO yang telah diisi.
        return positionDTOs;
    }

    // Metode untuk mengonversi list objek TalentSkillset menjadi list objek DTO (SkillsetDTO).
    private List<SkillsetDTO> mapSkillsets(List<TalentSkillset> talentSkillsets) {
        // Membuat list baru untuk menyimpan objek DTO hasil konversi
        List<SkillsetDTO> skillsetDTOs = new ArrayList<>();

        // Iterasi melalui setiap objek TalentSkillset dalam list talentSkillsets
        for (TalentSkillset talentSkillset : talentSkillsets) {
            // Memeriksa apakah objek TalentSkillset dan objek Skillset tidak null
            if (talentSkillset != null && talentSkillset.getSkillset() != null) {
                // Membuat objek DTO baru dengan menggunakan konstruktor yang menerima ID dan nama Skillset
                SkillsetDTO skillsetDTO = new SkillsetDTO(
                        talentSkillset.getSkillset().getSkillsetId(),
                        talentSkillset.getSkillset().getSkillsetName()
                );

                // Menambahkan objek DTO ke daftar hasil konversi
                skillsetDTOs.add(skillsetDTO);
            }
        }

        // Mengembalikan list objek DTO yang telah diisi
        return skillsetDTOs;
    }

}