package com.tujuhsembilan.app.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "talent")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Talent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "talent_id")
    private UUID talentId;

    @ToString.Exclude // Exclude from toString
    @EqualsAndHashCode.Exclude // Exclude from hashCode and equals
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "talent")
    private List<TalentPosition> talentPositions;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "talent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TalentSkillset> talentSkillsets;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "talent_level_id")
    private TalentLevel talentLevel;

    @OneToOne
    @JoinColumn(name = "talent_status_id")
    private TalentStatus talentStatus;

    @ToString.Exclude
    @OneToOne(mappedBy = "talent", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private TalentMetadata talentMetadata;

    @ManyToOne
    @JoinColumn(name = "employee_status_id", referencedColumnName = "employee_status_id")
    private EmployeeStatus employeeStatus;

    @Size(max = 255)
    @Column(name = "talent_name")
    private String talentName;

    @Size(max = 255)
    @Column(name = "talent_photo_filename")
    private String talentPhotoFilename;

    @Size(max = 50)
    @Column(name = "employee_number")
    private String employeeNumber;

    @Size(max = 1)
    @Column(name = "gender", columnDefinition = "bpchar(1)")
    private String gender;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "talent_description")
    private String talentDescription;

    @Size(max = 255)
    @Column(name = "talent_cv_filename")
    private String talentCVFilename;

    @Column(name = "experience")
    private Integer talentExperience;

    @Size(max = 100)
    @Column(name = "email")
    private String email;

    @Size(max = 20)
    @Column(name = "cellphone")
    private String cellphone;

    @Column(name = "biography_video_url")
    private String biographyVideoUrl;

    @Column(name = "is_add_to_list_enable")
    private Boolean isAddToListEnable;

    @Column(name = "talent_availability")
    private Boolean talentAvailability;

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

    @Size(max = 1)
    @Column(name = "sex", columnDefinition = "bpchar(1)")
    private String sex;

    @Column(name = "talent_cv_url")
    private String talentCvUrl;

    @Column(name = "talent_photo_url")
    private String talentPhotoUrl;

    @Column(name = "total_project_completed")
    private Integer totalProjectCompleted;

    // Business method to check availability
    public boolean isAvailable() {
        return Boolean.TRUE.equals(this.talentAvailability);
    }

    // Business method to set availability
    public void setAvailability(boolean availability) {
        this.talentAvailability = availability;
    }
}