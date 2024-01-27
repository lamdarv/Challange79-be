package com.tujuhsembilan.app.controller;

import com.tujuhsembilan.app.dto.UserRegistrationDTO;
import com.tujuhsembilan.app.service.DisplayRequestTalentService;
import com.tujuhsembilan.app.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/user-management/users")
public class UserController {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(DisplayRequestTalentService.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationDTO request) {
        log.info("Received request to register user: {}", request.getEmail());
        return userService.registerUser(request);
    }

    @PostMapping("/sign-in")
    @Transactional
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody Map<String, String> credentials) {
        log.info("Received sign-in request for email: {}", credentials.get("email"));
        String email = credentials.get("email");
        String password = credentials.get("password");

        return userService.signIn(email, password);
    }

}