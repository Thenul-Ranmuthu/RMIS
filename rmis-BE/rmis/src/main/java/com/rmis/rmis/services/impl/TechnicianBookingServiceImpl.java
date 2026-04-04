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
import com.rmis.rmis.services.interfaces.TechnicianBookingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
@Slf4j
public class TechnicianBookingServiceImpl implements TechnicianBookingService
{
    private final ServiceTicketRepository serviceTicketRepository;
    private final TechnicianRepository technicianRepository;
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
        dto.setServiceType(t.getServiceType());
        dto.setDescription(t.getDescription());
        Availability slot = t.getAvailability();
        dto.setAvailabilityId(slot.getId());
        dto.setScheduledDate(slot.getDate());
        dto.setScheduledStartTime(slot.getStartTime());
        dto.setScheduledEndTime(slot.getEndTime());
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
