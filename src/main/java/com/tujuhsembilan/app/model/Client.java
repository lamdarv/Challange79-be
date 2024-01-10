package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
//import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.hibernate.annotations.GenericGenerator;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedBy;
//import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Client {
    @Id
    @Column(name = "client_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID clientId;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_position_id")
    private ClientPosition clientPosition;

    @OneToOne
    @JoinColumn(name = "user_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private User user;

    @Column(name = "agency_address", length = 255)
    private String agencyAddress;

    @Column(name = "agency_name", length = 255)
    private String agencyName;

    @Column(name = "birth_date", columnDefinition = "timestamp")
    private LocalDateTime birthDate;

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "gender", length = 255)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "created_time",  columnDefinition = "timestamp")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time",  columnDefinition = "timestamp")
    private LocalDateTime lastModifiedTime;

    public enum Gender {
        Male,
        Female,
        L,
        P,
    }

}
