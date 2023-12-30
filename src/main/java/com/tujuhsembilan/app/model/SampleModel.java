package com.tujuhsembilan.app.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SampleModel {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    @Size(min = 8, max = 32)
    private String code;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private LocalDateTime date;

    // --- AUDIT ---

    @Column
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Long createdDate;

    @Column
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Long lastModifiedDate;

}