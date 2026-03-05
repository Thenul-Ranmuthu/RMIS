package com.rmis.rmis.domain.entities;

import com.rmis.rmis.domain.enums.QuotaRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "quota_requests",
        indexes = {
                @Index(name = "idx_quota_requests_status", columnList = "status"),
                @Index(name = "idx_quota_requests_company_name", columnList = "company_name"),
                @Index(name = "idx_quota_requests_submission_date", columnList = "submission_date DESC"),
                @Index(name = "idx_quota_requests_status_date", columnList = "status, submission_date DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaRequest {
    // Primary Key

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    // Company Reference

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quota_requests_company")
    )
    private Company company;

    // Denormalized from Company.name for fast dashboard reads
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    // Request Details

    @Column(name = "requested_quota", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedQuota;

    @Column(name = "submission_date", nullable = false, updatable = false)
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private QuotaRequestStatus status = QuotaRequestStatus.PENDING;

    // Review Details (populated on approval / rejection)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", foreignKey = @ForeignKey(name = "fk_quota_requests_reviewer"))
    private Admin reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

//    @Column(name = "review_notes", columnDefinition = "TEXT")
//    private String reviewNotes;

    // Audit Fields

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Lifecycle Hook

    @PrePersist
    protected void prePersist() {
        if (submissionDate == null) {
            submissionDate = LocalDateTime.now();
        }
    }
}
