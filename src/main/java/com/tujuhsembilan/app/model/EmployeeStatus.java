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
@Table(name = "employee_status")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class EmployeeStatus {
    @Id
    @GeneratedValue
    @Column(name = "employee_status_id")
    private UUID employeeStatusId;

    @Column(name = "employee_status_name", length = 50)
    private String employeeStatusName;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time", columnDefinition = "timestamp with time zone")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time", columnDefinition = "timestamp with time zone")
    private LocalDateTime lastModifiedTime;
}