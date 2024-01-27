package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.TalentWishlistDTO;
import com.tujuhsembilan.app.dto.talentRequest.WishlistItemDTO;
import com.tujuhsembilan.app.dto.talentRequest.WishlistResponseDTO;
import com.tujuhsembilan.app.exception.UserNotFoundException;
import com.tujuhsembilan.app.model.*;
import com.tujuhsembilan.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TalentWishlistService {
    private final TalentWishlistRepository talentWishlistRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TalentRepository talentRepository;
    private final TalentRequestRepository talentRequestRepository;
    private final TalentRequestStatusRepository talentRequestStatusRepository;
    private final JwtUtils jwtUtils;

    @Autowired
    public TalentWishlistService(
            TalentWishlistRepository talentWishlistRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            TalentRepository talentRepository,
            TalentRequestRepository talentRequestRepository,
            TalentRequestStatusRepository talentRequestStatusRepository,
            JwtUtils jwtUtils
    ) {
        this.talentWishlistRepository = talentWishlistRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.talentRepository = talentRepository;
        this.talentRequestRepository = talentRequestRepository;
        this.talentRequestStatusRepository = talentRequestStatusRepository;
        this.jwtUtils = jwtUtils;
    }

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    // Add Talent To Wishlist
    @Transactional
    public String addTalentToWishlist(TalentWishlistDTO talentWishlistDTO, HttpServletRequest request) {
        try {
            // Mendapatkan userID dari token
            UUID userId = getUserIdFromToken(request);

            // Memeriksa apakah userID valid
            if (userId == null) {
                return "Invalid token.";
            }

            // Mencari objek User berdasarkan userID
            User user = userRepository.findById(userId)
                    // Throw ResponseStatusException dengan status NOT_FOUND jika tidak ditemukan
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Memastikan bahwa User memiliki Client terkait
            if (user.getClient() == null) {
                return "User does not have an associated client.";
            }

            // Mendapatkan clientID dari objek Client yang terkait dengan User
            UUID clientId = user.getClient().getClientId();

            // Mencari objek Client berdasarkan clientID
            Client client = clientRepository.findById(clientId)
                    // Throw ResponseStatusException dengan status NOT_FOUND jika tidak ditemukan
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

            // Mencari objek Talent berdasarkan talentID dari objek TalentWishlistDTO
            Talent talent = talentRepository.findById(talentWishlistDTO.getTalentId())
                    // Throw ResponseStatusException dengan status NOT_FOUND jika tidak ditemukan
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talent not found"));

            // Mencari objek TalentWishlist berdasarkan talentID dan clientID
            TalentWishlist existingWishlistItem = talentWishlistRepository.findByTalentAndClient(talent, client);

            // Memeriksa apakah talent sudah ada di dalam daftar keinginan
            if (existingWishlistItem != null) {
                // Mengembalikan pesan bahwa talent sudah ada di dalam daftar keinginan
                return talent.getTalentName() + " with talentId " + talent.getTalentId() + " is already in the wishlist! The wishlistId is: " + existingWishlistItem.getTalentWishlistId();
            }

            // Membuat objek TalentWishlist dengan menggunakan builder pattern
            TalentWishlist talentWishlist = TalentWishlist.builder()
                    .talent(talent)
                    .client(client)
                    .wishlistDate(LocalDateTime.now())
                    .isActive(true)
                    .build();

            // Menyimpan objek TalentWishlist ke dalam repositori
            talentWishlistRepository.save(talentWishlist);

            // Mengembalikan pesan sukses yang berisi informasi tentang bakat yang berhasil ditambahkan ke dalam daftar keinginan
            return talent.getTalentName() + " with talentId " + talent.getTalentId() + " successfully added to wishlist! The wishlistId is: " + talentWishlist.getTalentWishlistId();

        } catch (Exception e) {
            // Throw RuntimeException jika terjadi kesalahan selama proses penambahan bakat ke dalam daftar keinginan
            throw new RuntimeException("Error adding talent to wishlist! " + e.getMessage(), e);
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest servletRequest) {
        // Mendapatkan token dari permintaan menggunakan JwtUtils
        String token = jwtUtils.extractTokenFromRequest(servletRequest);

        // Memvalidasi token dan mendapatkan UserID jika token valid
        return (token != null && jwtUtils.validateToken(token)) ? jwtUtils.getUserIdFromToken(token) : null;
    }


    @Transactional
    public void removeWishlist(UUID wishlistId) {
        // Opsional: memeriksa apakah wishist item ada sebelum dinonaktifkan
        TalentWishlist wishlistItem = talentWishlistRepository.findById(wishlistId)
                // Throw ResponseStatusException dengan status NOT_FOUND jika tidak ditemukan
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found"));

        // Memeriksa apakah wishlist sudah dinonaktifkan sebelumnya
        if (!wishlistItem.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wishlist item already deactivated");
        }

        // Menonaktifkan item daftar keinginan menggunakan metode khusus di repositori
        talentWishlistRepository.deactivateWishlist(wishlistId);
    }

    @Transactional
    public void removeAllWishlistByUserId(UUID userId) throws UserNotFoundException {
        // Search pengguna berdasarkan ID.
        Optional<User> optionalUser = userRepository.findById(userId);

        // Memeriksa apakah pengguna ditemukan, jika tidak, lemparkan exception UserNotFoundException.
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found for ID: " + userId);
        }

        // Mendapatkan objek pengguna dari Optional.
        User user = optionalUser.get();

        // Mendapatkan ID klien dari objek pengguna.
        UUID clientId = user.getClient().getClientId();

        // Memanggil metode service untuk menghapus semua daftar keinginan berdasarkan ID klien.
        removeAllWishlist(clientId);
    }
    @Transactional
    private void removeAllWishlist(UUID clientId) {
        // Mengambil semua wishlists berdasarkan ID client.
        List<TalentWishlist> wishlists = talentWishlistRepository.findAllByClient_ClientId(clientId);

        // Iterasi setiap wishlists
        for (TalentWishlist wishlist : wishlists) {
            // Memeriksa apakah wishlist aktif.
            if (wishlist.getIsActive()) {
                // Deactivate wishlist dengan mengakses metode repository khusus.
                talentWishlistRepository.deactivateWishlist(wishlist.getTalentWishlistId());
            }
        }
    }

    @Transactional
    public WishlistResponseDTO handleNewWishlistRequest(List<WishlistItemDTO> wishlistItems) {
        // List untuk menyimpan ID wishlist yang gagal diproses
        List<UUID> failedRequests = new ArrayList<>();

        // Iterasi melalui daftar item wishlist
        for (WishlistItemDTO item : wishlistItems) {
            log.info("Processing wishlist item with ID: {}", item.getWishlistId());

            // Mencari wishlist berdasarkan ID, jika tidak ditemukan, lemparkan EntityNotFoundException
            TalentWishlist wishlist = talentWishlistRepository.findById(item.getWishlistId())
                    .orElseThrow(() -> new EntityNotFoundException("Wishlist not found for ID: " + item.getWishlistId()));

            log.info("Found wishlist item: {}", wishlist);

            // Memeriksa apakah wishlist ada dan bakat tersedia
            if (wishlist != null && wishlist.getTalent().isAvailable()) {
                // Menonaktifkan is_active di TalentWishlist menjadi false
                wishlist.deactivate();
                talentWishlistRepository.save(wishlist);

                // Menonaktifkan talent_availability di Talent menjadi false
                Talent talent = wishlist.getTalent();
                talent.setAvailability(false);
                talentRepository.save(talent);

                // Membuat dan menyimpan permintaan bakat baru
                TalentRequestStatus onProgressStatus = talentRequestStatusRepository
                        .findByTalentRequestStatusName("On Progress")
                        .orElseThrow(() -> new IllegalStateException("On Progress status not found"));

                // Menyusun data untuk talent_request
                TalentRequest talentRequest = new TalentRequest();
                talentRequest.setRequestDate(LocalDateTime.now());
                talentRequest.setTalentRequestStatus(onProgressStatus);
                talentRequest.setTalentWishlist(wishlist);
                log.info("Saving talent request: {}", talentRequest);
                talentRequestRepository.save(talentRequest);
                log.info("Talent request saved with ID: {}", talentRequest.getTalentRequestId());
            } else {
                failedRequests.add(item.getWishlistId());
            }
        }

        // Membuat pesan berdasarkan apakah ada wishlist yang gagal diproses
        String message = failedRequests.isEmpty() ?
                "All talent requests processed successfully." :
                "Some talent requests failed to process.";

        // Mengembalikan objek WishlistResponseDTO
        return new WishlistResponseDTO(failedRequests, message);
    }

}
