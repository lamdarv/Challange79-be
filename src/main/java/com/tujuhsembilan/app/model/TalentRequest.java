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
@Table(name = "talent_request")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class TalentRequest {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "talent_request_id", updatable = false, nullable = false)
    private UUID talentRequestId;

    @ManyToOne
    @JoinColumn(name = "talent_request_status_id", referencedColumnName = "talent_request_status_id")
    private TalentRequestStatus talentRequestStatus;

    public void setTalentRequestStatus(TalentRequestStatus talentRequestStatus) {
        this.talentRequestStatus = talentRequestStatus;
    }

    @ManyToOne
    @JoinColumn(name = "talent_wishlist_id", referencedColumnName = "talent_wishlist_id")
    private TalentWishlist talentWishlist;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "request_reject_reason", length = 255)
    private String requestRejectReason;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_time")
    private LocalDateTime lastModifiedTime;
}
