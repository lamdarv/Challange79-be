package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.configuration.JwtUtils;
//import com.tujuhsembilan.app.controller.dto.*;
import com.tujuhsembilan.app.dto.UserRegistrationDTO;
import com.tujuhsembilan.app.model.Client;
import com.tujuhsembilan.app.model.Client.Gender;
import com.tujuhsembilan.app.model.ClientPosition;
import com.tujuhsembilan.app.model.Role;
import com.tujuhsembilan.app.model.User;
import com.tujuhsembilan.app.repository.ClientPositionRepository;
import com.tujuhsembilan.app.repository.ClientRepository;
import com.tujuhsembilan.app.repository.RoleRepository;
import com.tujuhsembilan.app.repository.UserRepository;
import com.tujuhsembilan.app.service.DisplayRequestTalentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/user-management/users")
public class UserController {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final ClientPositionRepository clientPositionRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    @Autowired
    public UserController(
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationDTO request) {
        log.info("Received request register for : " + request.getEmail());
        try {
            // Check for email duplication
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            } else {
                log.info("Email memang belum pernah didaftarkan");
            }

            // Find the role with name "Client"
            Role defaultRole = roleRepository.findFirstByRoleName("Client");

            if (defaultRole == null) {
                return ResponseEntity.badRequest().body("Error: Role 'Client' not found");
            } else {
                log.info("Role 'Client' ditemukan");
            }

            // Find ClientPosition by name
            String clientPositionName = request.getClientPositionName();
            log.debug("Attempting to find ClientPosition by name: {}", clientPositionName);
            Optional<ClientPosition> optionalClientPosition = clientPositionRepository.findByClientPositionName(clientPositionName);

            if (optionalClientPosition.isEmpty()) {
                log.warn("Client Position not found for name: {}", clientPositionName);
                return ResponseEntity.badRequest().body("Client Position not found");
            } else {
                log.info("ClientPosition found: {}", clientPositionName);
            }

            // Create a new User
//            log.debug("Creating new user with email: {}", request.getEmail());
            User newUser = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(defaultRole)
                    .createdTime(LocalDateTime.now())
                    .build();

            // Save the User to the database
            log.debug("Saving new user to database with email: {}", request.getEmail());
            User savedUser = userRepository.save(newUser);
            log.info("New user saved with ID: {}", savedUser.getUserId());

            // Get ClientPosition ID
            ClientPosition clientPosition = optionalClientPosition.get();

            // Create a new Client
            log.debug("Creating new client for user ID: {}", savedUser.getUserId());
            Client newClient = Client.builder()
                    .clientName(request.getFirstName() + " " + request.getLastName())
                    .email(request.getEmail())
                    .agencyName(request.getAgencyName())
                    .agencyAddress(request.getAgencyAddress())
                    .clientPosition(clientPosition)
                    .gender(Gender.valueOf(request.getGender()))
                    .birthDate(request.getBirthDate())
                    .createdTime(LocalDateTime.now())
                    .build();

            newClient.setUser(savedUser);
            clientRepository.save(newClient);

            return ResponseEntity.ok("User registered with ID: " + savedUser.getUserId());
        } catch (DataIntegrityViolationException e) {
            // This exception occurs when there is a violation of database constraints (e.g., unique key constraint)
            return ResponseEntity.badRequest().body("Error: User registration failed. Please check your input.");
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            log.error("Unexpected error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user");
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        log.info("Received login request with email: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Autentikasi
            if (passwordEncoder.matches(password, user.getPassword())) {
                UUID userId = user.getUserId();

                // Generate JWT token
                String token = jwtUtils.generateToken(user);

                // Extract role_id and client_id from the Role and Client object
                UUID roleId = user.getRole().getRoleId();
                UUID clientId = user.getClient().getClientId();

                // Respons
                Map<String, Object> response = new HashMap<>();
                response.put("email", user.getEmail());
                response.put("user_id", user.getUserId());
                response.put("client_id", clientId);
                response.put("role_id", roleId);
                response.put("token", token);

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
//                throw new RuntimeException("Invalid password");
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } else {
//            throw new RuntimeException("User not found");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

}