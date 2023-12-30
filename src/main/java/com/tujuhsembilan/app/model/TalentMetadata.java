package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "talent_metadata")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class TalentMetadata {
    @Id
    private UUID id;

    @MapsId // This annotation tells JPA to use the id field of Talent as the id of TalentMetadata
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_id")
    private Talent talent;

    @Column(name = "cv_counter", nullable = false)
    private Integer cvCounter;

    @Column(name = "profile_counter", nullable = false)
    private Integer profileCounter;

    @Column(name = "total_project_completed", nullable = false)
    private Integer totalProjectCompleted;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_time", nullable = false, columnDefinition = "timestamp with time zone")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", nullable = false, length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time", nullable = false, columnDefinition = "timestamp with time zone")
    private LocalDateTime lastModifiedTime;
}
