package com.tujuhsembilan.app.service;

import com.tujuhsembilan.app.configuration.JwtUtils;
import com.tujuhsembilan.app.dto.UserRegistrationDTO;
import com.tujuhsembilan.app.model.Client;
import com.tujuhsembilan.app.model.ClientPosition;
import com.tujuhsembilan.app.model.Role;
import com.tujuhsembilan.app.model.User;
import com.tujuhsembilan.app.repository.ClientPositionRepository;
import com.tujuhsembilan.app.repository.ClientRepository;
import com.tujuhsembilan.app.repository.RoleRepository;
import com.tujuhsembilan.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final ClientPositionRepository clientPositionRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);
    @Autowired
    public UserService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            RoleRepository roleRepository,
            ClientPositionRepository clientPositionRepository,
            JwtUtils jwtUtils,
            BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.clientPositionRepository = clientPositionRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    //For Register
    @Transactional
    public ResponseEntity<?> registerUser(UserRegistrationDTO request) {
        log.info("Processing registration for : {}", request.getEmail());
        try {
            // Check untuk duplikasi email
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Cari role dengan nama "Client"
            Role defaultRole = roleRepository.findFirstByRoleName("Client");
            if (defaultRole == null) {
                return ResponseEntity.badRequest().body("Error: Role 'Client' not found");
            }

            // Cari ClientPosition berdasarkan nama
            Optional<ClientPosition> optionalClientPosition = clientPositionRepository.findByClientPositionName(request.getClientPositionName());
            if (optionalClientPosition.isEmpty()) {
                return ResponseEntity.badRequest().body("Client Position not found");
            }

            // Buat dan simpan user baru
            User newUser = createUser(request, defaultRole);
            User savedUser = userRepository.save(newUser);

            // Buat dan simpan client baru
            Client newClient = createClient(request, savedUser, optionalClientPosition.get());
            clientRepository.save(newClient);

            // Mengembalikan respons sukses jika registrasi berhasil
            return ResponseEntity.ok("User registered with ID: " + savedUser.getUserId());
        } catch (DataIntegrityViolationException e) {
            // Menangani kesalahan integritas data, misalnya, jika ada keterbatasan unik pada database
            return ResponseEntity.badRequest().body("Error: User registration failed. Please check your input.");
        } catch (Exception e) {
            // Menangani kesalahan umum yang tidak terduga
            log.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user");
        }
    }

    //For Register
    @Transactional
    private User createUser(UserRegistrationDTO request, Role defaultRole) {
        // Membuat dan mengembalikan objek User baru menggunakan builder pattern
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(defaultRole)
                .createdTime(LocalDateTime.now())
                .build();
    }

    //For Register
    @Transactional
    private Client createClient(UserRegistrationDTO request, User savedUser, ClientPosition clientPosition) {
        // Membuat dan mengembalikan objek Client baru menggunakan builder pattern
        Client newClient = Client.builder()
                .clientName(request.getFirstName() + " " + request.getLastName())
                .email(request.getEmail())
                .agencyName(request.getAgencyName())
                .agencyAddress(request.getAgencyAddress())
                .clientPosition(clientPosition)
                .gender(Client.Gender.valueOf(request.getGender()))
                .birthDate(request.getBirthDate())
                .createdTime(LocalDateTime.now())
                .build();
        newClient.setUser(savedUser);
        return newClient;
    }

    // For SignIn
    @Transactional
    public ResponseEntity<Map<String, Object>> signIn(String email, String password) {
        log.info("Processing sign-in for email: {}", email);

        // Mencari user berdasarkan email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Jika user ditemukan berdasarkan email
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Memeriksa apakah password yang diinput sesuai dengan password di database
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Jika sesuai, mengembalikan respons sukses sign-in
                return ResponseEntity.ok(createSuccessSignInResponse(user));
            } else {
                // Jika password tidak sesuai, mengembalikan respons kesalahan dengan status UNAUTHORIZED
                return createErrorResponse("Invalid password", HttpStatus.UNAUTHORIZED);
            }
        } else {
            // Jika user tidak ditemukan berdasarkan email, mengembalikan respons kesalahan dengan status NOT_FOUND
            return createErrorResponse("User not found", HttpStatus.NOT_FOUND);
        }
    }

    // For SignIn
    @Transactional
    private Map<String, Object> createSuccessSignInResponse(User user) {
        // Generate token JWT menggunakan utilitas jwtUtils
        String token = jwtUtils.generateToken(user);

        // Mendapatkan roleId dan clientId dari objek User
        UUID roleId = user.getRole().getRoleId();
        UUID clientId = user.getClient().getClientId();

        // Membuat objek Map sebagai respons sign-in yang berhasil
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("user_id", user.getUserId());
        response.put("client_id", clientId);
        response.put("role_id", roleId);
        response.put("token", token);
        return response;
    }

    // For SignIn
    @Transactional
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        // Membuat objek Map untuk menyimpan pesan kesalahan
        Map<String, Object> response = new HashMap<>();

        // Menambahkan pesan kesalahan ke objek Map
        response.put("message", message);

        // Mengembalikan ResponseEntity yang berisi objek Map dan status kesalahan yang diberikan
        return new ResponseEntity<>(response, status);
    }
}
