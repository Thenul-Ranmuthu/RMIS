package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;
import com.rmis.rmis.domain.entities.*;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.repositories.*;
import com.rmis.rmis.services.interfaces.ServiceTicketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ServiceTicketServiceImpl implements ServiceTicketService {

    private final ServiceTicketRepository serviceTicketRepository;
    private final AvailabilityRepository   availabilityRepository;
    private final PublicUserRepository     publicUserRepository;
    private final CompanyRepository        companyRepository;

    // Simple in-memory sequence; safe for single-instance deployments.
    // For multi-instance, replace with a DB sequence or UUID suffix.
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    // ── Public-user booking ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ServiceTicketResponseDto raiseTicketAsPublicUser(String userEmail,
                                                            ServiceTicketRequestDto dto) {
        PublicUser user = publicUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Availability slot = getAndLockSlot(dto.getAvailabilityId());

        ServiceTicket ticket = buildTicket(dto, slot);
        ticket.setPublicUser(user);

        ServiceTicket saved = serviceTicketRepository.save(ticket);
        log.info("Service ticket {} created by public user {}", saved.getTicketNumber(), userEmail);
        return toResponseDto(saved);
    }

    // ── Company booking ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ServiceTicketResponseDto raiseTicketAsCompany(String companyEmail,
                                                         ServiceTicketRequestDto dto) {
        Company company = companyRepository.findByEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Availability slot = getAndLockSlot(dto.getAvailabilityId());

        ServiceTicket ticket = buildTicket(dto, slot);
        ticket.setCompany(company);

        ServiceTicket saved = serviceTicketRepository.save(ticket);
        log.info("Service ticket {} created by company {}", saved.getTicketNumber(), companyEmail);
        return toResponseDto(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<ServiceTicketResponseDto> getMyTicketsAsPublicUser(String userEmail) {
        PublicUser user = publicUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return serviceTicketRepository
                .findByPublicUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicketResponseDto> getMyTicketsAsCompany(String companyEmail) {
        Company company = companyRepository.findByEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return serviceTicketRepository
                .findByCompanyIdOrderByCreatedAtDesc(company.getId())
                .stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public ServiceTicketResponseDto getTicketById(Long ticketId) {
        return toResponseDto(serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Service ticket not found")));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Validates the slot is AVAILABLE then atomically marks it BOOKED.
     * Throws IllegalStateException on any double-booking attempt (Scenario 2).
     */
    @Transactional
    protected Availability getAndLockSlot(Long availabilityId) {
        Availability slot = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability slot not found with ID: " + availabilityId));

        // Layer 1: check the slot's own status flag
        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new IllegalStateException(
                    "This time slot is no longer available. Please choose a different slot.");
        }

        // Layer 2: check no ticket already references this slot
        if (serviceTicketRepository.existsByAvailabilityId(availabilityId)) {
            throw new IllegalStateException(
                    "This time slot has already been booked. Please choose a different slot.");
        }

        slot.setStatus("BOOKED");
        availabilityRepository.save(slot);
        return slot;
    }

    /** Builds a ServiceTicket without setting the customer — caller sets that. */
    private ServiceTicket buildTicket(ServiceTicketRequestDto dto, Availability slot) {
        ServiceTicket ticket = new ServiceTicket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setTechnician(slot.getTechnician());
        ticket.setAvailability(slot);
        ticket.setServiceType(dto.getServiceType());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(ServiceTicketStatus.PENDING);
        ticket.setSubmissionDate(LocalDate.now());
        return ticket;
    }

    /** Generates a readable ticket number, e.g. ST-20260403-0001. */
    private String generateTicketNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("ST-%s-%04d", date, SEQUENCE.getAndIncrement());
    }

    private ServiceTicketResponseDto toResponseDto(ServiceTicket t) {
        ServiceTicketResponseDto dto = new ServiceTicketResponseDto();
        dto.setId(t.getId());
        dto.setTicketNumber(t.getTicketNumber());
        dto.setStatus(t.getStatus().name());
        dto.setServiceType(t.getServiceType());
        dto.setDescription(t.getDescription());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());

        // Customer
        if (t.getPublicUser() != null) {
            PublicUser u = t.getPublicUser();
            dto.setCustomerName(u.getFirstName() + " " + u.getLastName());
            dto.setCustomerEmail(u.getEmail());
            dto.setCustomerType("PUBLIC_USER");
        } else if (t.getCompany() != null) {
            dto.setCustomerName(t.getCompany().getName());
            dto.setCustomerEmail(t.getCompany().getEmail());
            dto.setCustomerType("COMPANY");
        }

        // Technician
        Technician tech = t.getTechnician();
        dto.setTechnicianId(tech.getId());
        dto.setTechnicianName(tech.getFirstName() + " " + tech.getLastName());
        dto.setTechnicianSpecialization(tech.getSpecialization());

        // Booked slot
        Availability slot = t.getAvailability();
        dto.setAvailabilityId(slot.getId());
        dto.setScheduledDate(slot.getDate());
        dto.setScheduledStartTime(slot.getStartTime());
        dto.setScheduledEndTime(slot.getEndTime());

        return dto;
    }
}