package com.rmis.rmis.domain.dtos;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
@Getter
@Setter
public class TechnicianBookingResponseDto {
    private Long id;
    private String ticketNumber;
    private String status;
    private LocalDateTime createdAt;
    private Long availabilityId;
    private LocalDate scheduledDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private String serviceType;
    private String description;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerType; // "PUBLIC_USER" or "COMPANY"
}
