package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;
import com.rmis.rmis.domain.entities.*;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.mappers.Mapper;
import com.rmis.rmis.repositories.*;
import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.ServiceTicketService;
import com.rmis.rmis.utils.TicketNumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceTicketServiceImpl implements ServiceTicketService {
    private final ServiceTicketRepository serviceTicketRepository;
    private final AvailabilityRepository   availabilityRepository;
    private final PublicUserRepository     publicUserRepository;
    private final CompanyRepository        companyRepository;
    private final EmailService             emailService;
    private final TicketNumberGenerator    ticketNumberGenerator;
    private final Mapper<ServiceTicket, ServiceTicketResponseDto> serviceTicketMapper;

    public ServiceTicketServiceImpl(ServiceTicketRepository serviceTicketRepository,
                                    AvailabilityRepository availabilityRepository,
                                    PublicUserRepository publicUserRepository,
                                    CompanyRepository companyRepository,
                                    @Lazy EmailService emailService,
                                    TicketNumberGenerator ticketNumberGenerator,
                                    Mapper<ServiceTicket, ServiceTicketResponseDto> serviceTicketMapper) {
        this.serviceTicketRepository = serviceTicketRepository;
        this.availabilityRepository = availabilityRepository;
        this.publicUserRepository = publicUserRepository;
        this.companyRepository = companyRepository;
        this.emailService = emailService;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.serviceTicketMapper = serviceTicketMapper;
    }


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

        try {
            emailService.sendBookingConfirmationEmail(saved);
        } catch (Exception e) {
            log.warn("Failed to send booking confirmation email: {}", e.getMessage());
        }

        return serviceTicketMapper.mapTo(saved);
    }


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

        try {
            emailService.sendBookingConfirmationEmail(saved);
        } catch (Exception e) {
            log.warn("Failed to send booking confirmation email: {}", e.getMessage());
        }

        return serviceTicketMapper.mapTo(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public List<ServiceTicketResponseDto> getMyTicketsAsPublicUser(String userEmail) {
        PublicUser user = publicUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return serviceTicketRepository
                .findByPublicUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(serviceTicketMapper::mapTo).collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicketResponseDto> getMyTicketsAsCompany(String companyEmail) {
        Company company = companyRepository.findByEmail(companyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return serviceTicketRepository
                .findByCompanyIdOrderByCreatedAtDesc(company.getId())
                .stream().map(serviceTicketMapper::mapTo).collect(Collectors.toList());
    }

    @Override
    public ServiceTicketResponseDto getTicketById(Long ticketId) {
        return serviceTicketMapper.mapTo(serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Service ticket not found")));
    }

    @Override
    @Transactional
    public ServiceTicketResponseDto cancelTicket(Long ticketId, String reason, String userEmail) {
        ServiceTicket ticket = serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Service ticket not found"));

        // Validation: Verify ownership (Simplified for now - can be more advanced)
        // If ticket.getPublicUser is not null, compare with userEmail. Same for company.
        if (ticket.getPublicUser() != null && !ticket.getPublicUser().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Unauthorized to cancel this ticket");
        } else if (ticket.getCompany() != null && !ticket.getCompany().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Unauthorized to cancel this ticket");
        }

        // Validation: Scenario 3 – Restrict Cancellation After Service Starts
        if (ticket.getStatus() != ServiceTicketStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel booking. Status is " + ticket.getStatus() +
                    ". Only Pending bookings can be cancelled.");
        }

        // Implementation: Scenario 1
        ticket.setStatus(ServiceTicketStatus.CANCELLED);
        ticket.setCancellationReason(reason);
        ticket.setCancellationTimestamp(LocalDateTime.now());

        // Implementation: Scenario 2 – Release Time Slot
        Availability slot = ticket.getAvailability();
        slot.setStatus("AVAILABLE");
        availabilityRepository.save(slot);

        ServiceTicket saved = serviceTicketRepository.save(ticket);
        log.info("Service ticket {} cancelled by {}", saved.getTicketNumber(), userEmail);

        // Scenario 4: Notify Technician & Scenario 1: Confirm to Customer
        try {
            emailService.sendBookingCancellationEmail(saved);
            emailService.sendBookingCancellationTechnicianEmail(saved);
        } catch (Exception e) {
            log.warn("Email notification failed for cancelled ticket {}: {}", saved.getTicketNumber(), e.getMessage());
        }

        return serviceTicketMapper.mapTo(saved);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Validates the slot is AVAILABLE then atomically marks it BOOKED.
     * Throws IllegalStateException on any double-booking attempt (Scenario 2).
     */
    @Transactional
    protected Availability getAndLockSlot(Long availabilityId) {
        Availability slot = availabilityRepository.findByIdWithLock(availabilityId)
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
        ticket.setTicketNumber(ticketNumberGenerator.generate());
        ticket.setTechnician(slot.getTechnician());
        ticket.setAvailability(slot);
        ticket.setServiceType(dto.getServiceType());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(ServiceTicketStatus.PENDING);
        ticket.setSubmissionDate(LocalDate.now());
        return ticket;
    }
}