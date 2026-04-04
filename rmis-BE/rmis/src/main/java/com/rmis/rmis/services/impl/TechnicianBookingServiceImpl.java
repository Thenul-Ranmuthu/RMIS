package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.TechnicianBookingResponseDto;
import com.rmis.rmis.domain.entities.Availability;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.domain.entities.ServiceTicket;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.repositories.ServiceTicketRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.TechnicianBookingService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.rmis.rmis.domain.dtos.BookingStatusUpdateRequestDto;
import java.util.List;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
@Slf4j
public class TechnicianBookingServiceImpl implements TechnicianBookingService
{
    private final ServiceTicketRepository serviceTicketRepository;
    private final TechnicianRepository technicianRepository;
    private final EmailService emailService;

    @Override
    public List<TechnicianBookingResponseDto> getMyBookings(String
                                                                    technicianEmail) {
        Technician technician = getByEmail(technicianEmail);
        return serviceTicketRepository
                .findByTechnicianIdOrderByCreatedAtDesc(technician.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<TechnicianBookingResponseDto> getMyBookingsByStatus(
            String technicianEmail, String status) {
        Technician technician = getByEmail(technicianEmail);
        ServiceTicketStatus statusEnum;
        try {
            statusEnum = ServiceTicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: PENDING, SCHEDULED, CANCELLED");
        }
        return serviceTicketRepository

                .findByTechnicianIdAndStatusOrderByCreatedAtDesc(technician.getId(),
                        statusEnum)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public TechnicianBookingResponseDto getBookingDetail(
            String technicianEmail, Long ticketId) {
        Technician technician = getByEmail(technicianEmail);
        ServiceTicket ticket = serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with ID: " + ticketId));
        // Ownership check — technician can only see their own bookings
        if (!ticket.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException(
                    "Access denied: this booking does not belong to you");
        }
        return toDto(ticket);
    }

    @Override
    @Transactional
    public TechnicianBookingResponseDto updateBookingStatus(
            String technicianEmail, Long ticketId, BookingStatusUpdateRequestDto dto) {

        Technician technician = getByEmail(technicianEmail);
        ServiceTicket ticket  = getTicket(ticketId);
        assertOwnership(ticket, technician);

        ServiceTicketStatus newStatus = parseStatus(dto.getStatus());
        ServiceTicketStatus current   = ticket.getStatus();

        validateTransition(current, newStatus);

        if (newStatus == ServiceTicketStatus.CANCELLED &&
                (dto.getCancellationReason() == null || dto.getCancellationReason().isBlank())) {
            throw new IllegalArgumentException("A cancellation reason is required.");
        }

        ticket.setStatus(newStatus);

        if (newStatus == ServiceTicketStatus.CANCELLED) {
            ticket.setCancellationReason(dto.getCancellationReason());
        }

        ServiceTicket saved = serviceTicketRepository.save(ticket);
        log.info("Ticket {} updated from {} to {} by {}", ticket.getTicketNumber(), current, newStatus, technicianEmail);

        try {
            if (newStatus == ServiceTicketStatus.CANCELLED) {
                emailService.sendBookingCancellationEmail(saved);
            } else {
                emailService.sendBookingStatusUpdateEmail(saved);
            }
        } catch (Exception e) {
            log.warn("Email notification failed for ticket {}: {}", ticket.getTicketNumber(), e.getMessage());
        }

        return toDto(saved);
    }

    private void validateTransition(ServiceTicketStatus current, ServiceTicketStatus next) {
        boolean allowed = switch (current) {
            case PENDING   -> next == ServiceTicketStatus.ACCEPTED  || next == ServiceTicketStatus.CANCELLED;
            case ACCEPTED  -> next == ServiceTicketStatus.COMPLETED || next == ServiceTicketStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalArgumentException(
                    "Cannot transition from " + current + " to " + next + ".");
        }
    }

    private ServiceTicket getTicket(Long ticketId) {
        return serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + ticketId));
    }

    private void assertOwnership(ServiceTicket ticket, Technician technician) {
        if (!ticket.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("Access denied: this booking does not belong to you");
        }
    }

    private ServiceTicketStatus parseStatus(String status) {
        try {
            return ServiceTicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed: ACCEPTED, COMPLETED, CANCELLED");
        }
    }

    private Technician getByEmail(String email) {
        return technicianRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));
    }
    private TechnicianBookingResponseDto toDto(ServiceTicket t) {
        TechnicianBookingResponseDto dto = new
                TechnicianBookingResponseDto();
        dto.setId(t.getId());
        dto.setTicketNumber(t.getTicketNumber());
        dto.setStatus(t.getStatus().name());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setSubmissionDate(t.getSubmissionDate());
        dto.setServiceType(t.getServiceType());
        dto.setDescription(t.getDescription());
        Availability slot = t.getAvailability();
        dto.setAvailabilityId(slot.getId());
        dto.setScheduledDate(slot.getDate());
        dto.setScheduledStartTime(slot.getStartTime());
        dto.setScheduledEndTime(slot.getEndTime());
        dto.setCancellationReason(t.getCancellationReason());
        if (t.getPublicUser() != null) {
            PublicUser u = t.getPublicUser();
            dto.setCustomerName(u.getFirstName() + " " + u.getLastName());
            dto.setCustomerEmail(u.getEmail());
            dto.setCustomerPhone(u.getPhone());
            dto.setCustomerType("PUBLIC_USER");
        } else if (t.getCompany() != null) {
            Company c = t.getCompany();
            dto.setCustomerName(c.getName());
            dto.setCustomerEmail(c.getEmail());
            dto.setCustomerPhone(null); // Company entity has no phone field
            dto.setCustomerType("COMPANY");
        }
        return dto;
    }
}
