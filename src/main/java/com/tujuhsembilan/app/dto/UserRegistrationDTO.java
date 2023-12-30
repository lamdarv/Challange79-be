package com.tujuhsembilan.app.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserRegistrationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String gender;
    private LocalDateTime birthDate;
    private UUID role;
    private String clientPositionName;
    private String agencyName;
    private String agencyAddress;
}
