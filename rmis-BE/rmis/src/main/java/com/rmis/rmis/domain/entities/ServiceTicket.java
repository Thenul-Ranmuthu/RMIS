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

    @Column(nullable = false, unique = true)
    private String ticketNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_user_id")
    private PublicUser publicUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = true)
    private Technician technician;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = true, unique = true)
    private Availability availability;


    @Column(nullable = false, length = 100)
    private String serviceType;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceTicketStatus status;

    @Column(length = 500)
    private String cancellationReason;

    private LocalDateTime cancellationTimestamp;

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