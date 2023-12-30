package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skillset")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Skillset {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "skillset_id", updatable = false, nullable = false)
    private UUID skillsetId;

    @OneToMany(mappedBy = "skillset", cascade = CascadeType.ALL)
    private List<MostFrequentSkillset> mostFrequentSkillsets;

    @PrePersist
    public void prePersist() {
        if (skillsetId == null) {
            skillsetId = UUID.randomUUID();
        }
    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "skillset_type_id", nullable = false)
//    private SkillsetType skillsetType;

    @Column(name = "skillset_type_id")
    private UUID skillsetTypeId;

    @Column(name = "skillset_name", length = 50)
    @Size(max = 50)
    private String skillsetName;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_time")
    private LocalDateTime lastModifiedTime;

}
