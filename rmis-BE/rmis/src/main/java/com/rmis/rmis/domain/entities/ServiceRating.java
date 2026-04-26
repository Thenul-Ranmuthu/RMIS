package com.rmis.rmis.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "service_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = "service_ticket_id")
})
public class ServiceRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_ticket_id", nullable = false, unique = true)
    private ServiceTicket serviceTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 1000)
    private String feedback;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
