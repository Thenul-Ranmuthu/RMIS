package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.*;
import com.rmis.rmis.domain.entities.Certification;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import com.rmis.rmis.domain.enums.SkillLevel;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.repositories.CertificationRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.repositories.ServiceTicketRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianAuthServiceImpl implements TechnicianAuthService {

    private static final Logger log = LoggerFactory.getLogger(TechnicianAuthServiceImpl.class);

    private final TechnicianRepository technicianRepository;
    private final CertificationRepository certificationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final JwtTokenProvider jwtTokenProvider;
    private final ServiceTicketRepository serviceTicketRepository;

    public TechnicianAuthServiceImpl(
            TechnicianRepository technicianRepository,
            CertificationRepository certificationRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            FileStorageServiceImpl fileStorageServiceImpl,
            @Qualifier("technicianAuthenticationProvider")
            AuthenticationProvider authenticationProvider,
            JwtTokenProvider jwtTokenProvider,
            ServiceTicketRepository serviceTicketRepository
    ) {
        this.technicianRepository = technicianRepository;
        this.certificationRepository = certificationRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageServiceImpl = fileStorageServiceImpl;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.serviceTicketRepository = serviceTicketRepository;
    }

    @Override
    @Transactional
    public TechnicianRegisterResponseDto register(TechnicianRegisterDto registerDto) {
        log.info("Attempting to register technician with email: {}", registerDto.getEmail());

        if (technicianRepository.existsByEmail(registerDto.getEmail())) {
            log.warn("Registration failed - email already exists: {}", registerDto.getEmail());
            throw new RegisterUserAlreadyExistsException("Email already registered!");
        }

        Role technicianRole = roleRepository.findByName("ROLE_TECHNICIAN");
        if (technicianRole == null) {
            log.error("Technician role not found in database");
            throw new RuntimeException("Technician role not found in database");
        }

        Technician technician = new Technician();
        technician.setFirstName(registerDto.getFirstName());
        technician.setLastName(registerDto.getLastName());
        technician.setEmail(registerDto.getEmail());
        technician.setPhoneNumber(registerDto.getPhoneNumber());
        technician.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        technician.setAddress(registerDto.getAddress());
        technician.setDistrict(registerDto.getDistrict());
        technician.setSpecialization(registerDto.getSpecialization());
        technician.setYearsOfExperience(registerDto.getYearsOfExperience());
        technician.setStatus("PENDING");
        technician.setRole(technicianRole);

        Technician savedTechnician = technicianRepository.save(technician);
        log.info("Technician saved with ID: {}", savedTechnician.getId());

        List<Certification> certifications = new ArrayList<>();
        if (registerDto.getCertifications() != null && !registerDto.getCertifications().isEmpty()) {
            for (CertificationDto certDto : registerDto.getCertifications()) {
                if (certDto.getFile() == null || certDto.getFile().isEmpty()) {
                    throw new RuntimeException("Certification file is required");
                }

                String filePath = fileStorageServiceImpl.storeCertification(certDto.getFile(), savedTechnician.getId());

                Certification certification = new Certification();
                certification.setCertificationName(certDto.getCertificationName());
                certification.setFilePath(filePath);
                certification.setOriginalFileName(certDto.getFile().getOriginalFilename());
                certification.setFileType(certDto.getFile().getContentType());
                certification.setFileSize(certDto.getFile().getSize());
                certification.setTechnician(savedTechnician);
                certification.setIssuingAuthority(certDto.getIssuingAuthority());

                certifications.add(certificationRepository.save(certification));
                log.info("Certification saved for technician: {}", savedTechnician.getId());
            }
        }

        savedTechnician.setCertifications(certifications);
        log.info("Technician registration completed successfully for: {}", registerDto.getEmail());

        TechnicianRegisterResponseDto response = new TechnicianRegisterResponseDto();
        response.setMsg("Registration successful. Pending admin approval.");
        return response;
    }

    @Override
    public Technician getPendingTechnicians(LoginDto loginDto) {
        Technician technician = technicianRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Technician not found: " + loginDto.getEmail()));
        if (technician.getStatus().equalsIgnoreCase("ACTIVE")) {
            return technician;
        } else {
            return null;
        }
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("Login attempt for technician: {}", loginDto.getEmail());

        if (!technicianRepository.existsByEmail(loginDto.getEmail())) {
            throw new UnregisteredUserException("User with this email does not exist or password is incorrect.");
        }

        Technician technician = technicianRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        if (!"ACTIVE".equals(technician.getStatus())) {
            log.warn("Login attempt by non-active technician: {}", loginDto.getEmail());
            throw new RuntimeException("Account is not active. Status: " + technician.getStatus());
        }

        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "TECHNICIAN");

        log.info("Technician logged in successfully: {}", loginDto.getEmail());
        return token;
    }

    @Override
    @Transactional
    public TechnicianResponseDto approveTechnician(Long technicianId, SkillLevel skillLevel) {
        log.info("Approving technician with ID: {}", technicianId);
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));
        technician.setStatus("ACTIVE");
        technician.setSkillLevel(skillLevel);
        technician.setApprovalDate(LocalDateTime.now());
        technician.setRejectionReason(null);
        return mapToResponseDto(technicianRepository.save(technician));
    }

    @Override
    @Transactional
    public TechnicianResponseDto rejectTechnician(Long technicianId, String reason) {
        log.info("Rejecting technician with ID: {}", technicianId);
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));
        technician.setStatus("REJECTED");
        technician.setRejectionReason(reason);
        return mapToResponseDto(technicianRepository.save(technician));
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianResponseDto getTechnicianById(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        return mapToResponseDto(technician);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicianResponseDto> getTechniciansByStatus(String status) {
        return technicianRepository.findByStatus(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    // In TechnicianAuthServiceImpl.deleteTechnician():

    @Override
    @Transactional
    public void deleteTechnician(Long id) {
        log.info("Deleting technician with ID: {}", id);
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));

        // Block deletion if any active tickets exist
        boolean hasActiveTickets = serviceTicketRepository
                .existsByTechnicianIdAndStatusIn(
                        id,
                        List.of(ServiceTicketStatus.PENDING, ServiceTicketStatus.ACCEPTED)
                );
        if (hasActiveTickets) {
            throw new IllegalStateException(
                    "Cannot delete technician with active service tickets. Resolve or cancel them first.");
        }

        // Nullify availability reference on all historical (cancelled/completed) tickets
        // so the FK constraint doesn't block the availability deletion
        serviceTicketRepository.findByTechnicianId(id).forEach(ticket -> {
            ticket.setAvailability(null);
            ticket.setTechnician(null);
            serviceTicketRepository.save(ticket);
        });

        if (technician.getCertifications() != null) {
            for (Certification cert : technician.getCertifications()) {
                fileStorageServiceImpl.deleteFile(cert.getFilePath());
            }
        }
        technicianRepository.delete(technician);
        log.info("Technician deleted successfully: {}", id);
    }

    @Override
    public List<TechnicianResponseDto> searchAvailableTechnicians(LocalDate date, SkillLevel skillLevel) {
        log.info("Searching available technicians for date: {}, skillLevel: {}", date, skillLevel);
        if (date != null) {
            return technicianRepository.findAvailableByDateAndSkillLevel(date, skillLevel)
                    .stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else {
            return technicianRepository.findActiveBySkillLevel(skillLevel, LocalDate.now())
                    .stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        }
    }

    private TechnicianResponseDto mapToResponseDto(Technician technician) {
        TechnicianResponseDto dto = new TechnicianResponseDto();
        dto.setId(technician.getId());
        dto.setFirstName(technician.getFirstName());
        dto.setLastName(technician.getLastName());
        dto.setEmail(technician.getEmail());
        dto.setPhoneNumber(technician.getPhoneNumber());
        dto.setAddress(technician.getAddress());
        dto.setDistrict(technician.getDistrict());
        dto.setSpecialization(technician.getSpecialization());
        dto.setYearsOfExperience(technician.getYearsOfExperience());
        dto.setSkillLevel(technician.getSkillLevel());
        dto.setStatus(technician.getStatus());
        dto.setRegistrationDate(technician.getRegistrationDate());
        dto.setApprovalDate(technician.getApprovalDate());

        if (technician.getCertifications() != null) {
            List<CertificationResponseDto> certDtos = technician.getCertifications().stream()
                    .map(cert -> {
                        CertificationResponseDto c = new CertificationResponseDto();
                        c.setId(cert.getId());
                        c.setCertificationName(cert.getCertificationName());
                        c.setIssuingAuthority(cert.getIssuingAuthority());
                        c.setFileType(cert.getFileType());
                        c.setOriginalFileName(cert.getOriginalFileName());
                        c.setFileUrl("/" + cert.getFilePath());
                        return c;
                    })
                    .collect(Collectors.toList());
            dto.setCertifications(certDtos);
        }

        return dto;
    }
}