package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.ServiceRatingRequestDto;
import com.rmis.rmis.domain.dtos.ServiceRatingResponseDto;
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
    private final ServiceRatingRepository  serviceRatingRepository;
    private final EmailService             emailService;
    private final TicketNumberGenerator    ticketNumberGenerator;
    private final Mapper<ServiceTicket, ServiceTicketResponseDto> serviceTicketMapper;

    public ServiceTicketServiceImpl(ServiceTicketRepository serviceTicketRepository,
                                    AvailabilityRepository availabilityRepository,
                                    PublicUserRepository publicUserRepository,
                                    CompanyRepository companyRepository,
                                    ServiceRatingRepository serviceRatingRepository,
                                    @Lazy EmailService emailService,
                                    TicketNumberGenerator ticketNumberGenerator,
                                    Mapper<ServiceTicket, ServiceTicketResponseDto> serviceTicketMapper) {
        this.serviceTicketRepository = serviceTicketRepository;
        this.availabilityRepository = availabilityRepository;
        this.publicUserRepository = publicUserRepository;
        this.companyRepository = companyRepository;
        this.serviceRatingRepository = serviceRatingRepository;
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
    public List<ServiceTicketResponseDto> getAllTickets() {
        return serviceTicketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(serviceTicketMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceTicketResponseDto> getAllTicketsByStatus(String status) {
        ServiceTicketStatus statusEnum;
        try {
            statusEnum = ServiceTicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed: PENDING, ACCEPTED, COMPLETED, CANCELLED");
        }
        return serviceTicketRepository.findByStatusOrderByCreatedAtDesc(statusEnum)
                .stream()
                .map(serviceTicketMapper::mapTo)
                .collect(Collectors.toList());
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

        // Layer 2: check no active ticket already references this slot
        boolean alreadyBooked = serviceTicketRepository.existsByAvailabilityIdAndStatusNot(
                availabilityId, ServiceTicketStatus.CANCELLED);
        if (alreadyBooked) {
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

    // ── Rating methods ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ServiceRatingResponseDto submitRating(Long ticketId, String userEmail, ServiceRatingRequestDto dto) {
        ServiceTicket ticket = serviceTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Service ticket not found"));

        // Scenario 2: Restrict Rating — service must be completed
        if (ticket.getStatus() != ServiceTicketStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot rate this service. Only completed services can be rated. Current status: " + ticket.getStatus());
        }

        // Verify ownership: the rating must come from the customer who raised the ticket
        boolean isOwner = false;
        String reviewerName = "Anonymous";
        if (ticket.getPublicUser() != null && ticket.getPublicUser().getEmail().equals(userEmail)) {
            isOwner = true;
            reviewerName = ticket.getPublicUser().getFirstName() + " " + ticket.getPublicUser().getLastName();
        } else if (ticket.getCompany() != null && ticket.getCompany().getEmail().equals(userEmail)) {
            isOwner = true;
            reviewerName = ticket.getCompany().getName();
        }

        if (!isOwner) {
            throw new IllegalStateException("You are not authorized to rate this service.");
        }

        // Prevent duplicate ratings
        if (serviceRatingRepository.existsByServiceTicketId(ticketId)) {
            throw new IllegalStateException("You have already submitted a rating for this service.");
        }

        // Scenario 1: Store Rating
        ServiceRating rating = new ServiceRating();
        rating.setServiceTicket(ticket);
        rating.setTechnician(ticket.getTechnician());
        rating.setRating(dto.getRating());
        rating.setFeedback(dto.getFeedback());

        ServiceRating saved = serviceRatingRepository.save(rating);
        
        // Link the rating back to the ticket to ensure the bidirectional relationship is updated
        ticket.setServiceRating(saved);
        serviceTicketRepository.save(ticket);
        
        log.info("Rating {} submitted for ticket {} by {}", saved.getRating(), ticket.getTicketNumber(), userEmail);

        return ServiceRatingResponseDto.builder()
                .id(saved.getId())
                .serviceTicketId(ticket.getId())
                .rating(saved.getRating())
                .feedback(saved.getFeedback())
                .createdAt(saved.getCreatedAt())
                .reviewerName(reviewerName)
                .build();
    }

    @Override
    public List<ServiceRatingResponseDto> getTechnicianFeedbacks(Long technicianId) {
        return serviceRatingRepository.findByTechnicianIdOrderByCreatedAtDesc(technicianId)
                .stream()
                .map(r -> {
                    String reviewerName = "Anonymous";
                    ServiceTicket ticket = r.getServiceTicket();
                    if (ticket.getPublicUser() != null) {
                        reviewerName = ticket.getPublicUser().getFirstName() + " " + ticket.getPublicUser().getLastName();
                    } else if (ticket.getCompany() != null) {
                        reviewerName = ticket.getCompany().getName();
                    }
                    return ServiceRatingResponseDto.builder()
                            .id(r.getId())
                            .serviceTicketId(ticket.getId())
                            .rating(r.getRating())
                            .feedback(r.getFeedback())
                            .createdAt(r.getCreatedAt())
                            .reviewerName(reviewerName)
                            .build();
                })
                .collect(Collectors.toList());
    }
}