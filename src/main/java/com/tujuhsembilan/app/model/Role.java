package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role", schema = "public")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Role {
    @Id
    @Column(name = "role_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roleId;

    @Column(name = "role_name", length = 255)
    private String roleName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time", columnDefinition = "timestamp")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time",  columnDefinition = "timestamp")
    private LocalDateTime lastModifiedTime;

}
