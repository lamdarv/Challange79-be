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
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skillset_type", schema = "public")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SkillsetType {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "skillset_type_id", updatable = false, nullable = false)
    private UUID skillsetTypeId;

//    @OneToMany(mappedBy = "skillsetType", fetch = FetchType.LAZY)
//    private Set<Skillset> skillsets;

    @Column(name = "skillset_type_name", nullable = false, length = 255)
    private String skillsetTypeName;

    @Column(name = "is_programming_skill")
    private Boolean isProgrammingSkill;

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
