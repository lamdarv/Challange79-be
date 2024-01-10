package com.tujuhsembilan.app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "talent_wishlist")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class TalentWishlist {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "talent_wishlist_id", updatable = false, nullable = false)
    private UUID talentWishlistId;

    @ToString.Exclude
    @OneToMany(mappedBy = "talentWishlist")
    private List<TalentRequest> talentRequests;

    @ManyToOne
    @JoinColumn(name = "talent_id", referencedColumnName = "talent_id")
    private Talent talent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "wishlist_date")
    private LocalDateTime wishlistDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "last_modified_time")
    private LocalDateTime lastModifiedTime;

    // Business method to deactivate
    public void deactivate() {
        this.isActive = false;
    }
}

