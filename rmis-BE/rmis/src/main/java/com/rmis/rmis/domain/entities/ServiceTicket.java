package com.rmis.rmis.domain.entities;

import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "service_tickets")
public class ServiceTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable reference, e.g. ST-20260403-0001
     */
    @Column(nullable = false, unique = true)
    private String ticketNumber;

    // ── Customer (one of these two will be set) ───────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_user_id")
    private PublicUser publicUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // ── Technician & booked slot ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    /**
     * The availability slot being booked.
     * Its status is set to BOOKED when this ticket is created.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false, unique = true)
    private Availability availability;

    // ── Service details ───────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String serviceType;

    @Column(length = 500)
    private String description;

    // ── Status & audit ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceTicketStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "submission_date", nullable = false)
    private LocalDate submissionDate;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
        if (status == null) {
            status = ServiceTicketStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}