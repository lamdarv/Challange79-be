package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_position")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ClientPosition {
    @Id
    @Column(name = "client_position_id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID clientPositionId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "client_position_name", length = 255)
    private String clientPositionName;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time",  columnDefinition = "timestamp")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time",  columnDefinition = "timestamp")
    private LocalDateTime lastModifiedTime;
}
