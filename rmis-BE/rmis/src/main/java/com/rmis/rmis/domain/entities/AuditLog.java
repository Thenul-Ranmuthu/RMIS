package com.rmis.rmis.domain.entities;

import com.rmis.rmis.domain.enums.AuditActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                // Index on timestamp — used for date range filtering
                @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp DESC"),
                // Index on officer name — useful for future filtering by officer
                @Index(name = "idx_audit_logs_officer", columnList = "officer_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "officer_name", nullable = false)
    private String officerName;

    @Column(name = "officer_email", nullable = false)
    private String officerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private AuditActionType actionType;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
