package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "talent_skillset")
@IdClass(TalentSkillset.class)
public class TalentSkillset {
    @Id
    @ManyToOne
    @JoinColumn(name = "talent_id")
    private Talent talent;

    @Id
    @ManyToOne
    @JoinColumn(name = "skillset_id")
    private Skillset skillset;

    @Data
    public class TalentSkillsetId implements Serializable {
        private UUID talent;
        private UUID skillset;
    }

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
