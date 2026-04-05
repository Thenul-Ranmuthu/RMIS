package com.rmis.rmis.domain.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class ServiceTicketResponseDto {

    private Long id;
    private String ticketNumber;

    // Customer info (one will be populated)
    private String customerName;
    private String customerEmail;
    private String customerType;        // "PUBLIC_USER" or "COMPANY"

    // Technician info
    private Long technicianId;
    private String technicianName;
    private String technicianSpecialization;

    // Booked slot info
    private Long availabilityId;
    private LocalDate scheduledDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;

    // Service details
    private String serviceType;
    private String description;

    // Status & audit
    private String status;
    private String cancellationReason;
    private LocalDateTime cancellationTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}