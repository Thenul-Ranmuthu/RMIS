package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.*;
import com.rmis.rmis.domain.entities.Certification;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.exceptions.UnauthorizedException;
import com.rmis.rmis.repositories.CertificationRepository;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TechnicianAuthServiceImpl implements TechnicianAuthService {

    private static final Logger log = LoggerFactory.getLogger(TechnicianAuthServiceImpl.class);

    private final TechnicianRepository technicianRepository;
    private final CompanyRepository companyRepository;
    private final CertificationRepository certificationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStorageService fileStorageService;

    public TechnicianAuthServiceImpl(
            TechnicianRepository technicianRepository,
            CompanyRepository companyRepository,
            CertificationRepository certificationRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            FileStorageService fileStorageService) {
        this.technicianRepository = technicianRepository;
        this.companyRepository = companyRepository;
        this.certificationRepository = certificationRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public TechnicianResponseDto register(TechnicianRegisterDto registerDto) {
        log.info("Attempting to register technician with email: {}", registerDto.getEmail());

        // Check if email already exists
        if (technicianRepository.existsByEmail(registerDto.getEmail()) || companyRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", registerDto.getEmail());
            throw new RegisterUserAlreadyExistsException("Email already registered!");
        }

        // Get technician role
        Role technicianRole = roleRepository.findByName("ROLE_TECHNITIAN");
        if (technicianRole == null) {
            log.error("Technician role not found in database");
            throw new RuntimeException("Technician role not found in database");
        }

        // Create technician entity
        Technician technician = new Technician();
        technician.setFirstName(registerDto.getFirstName());
        technician.setLastName(registerDto.getLastName());
        technician.setEmail(registerDto.getEmail());
        technician.setPhoneNumber(registerDto.getPhoneNumber());
        technician.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        technician.setAddress(registerDto.getAddress());
        technician.setSpecialization(registerDto.getSpecialization());
        technician.setYearsOfExperience(registerDto.getYearsOfExperience());
        technician.setStatus("PENDING_APPROVAL");
        technician.setRegistrationDate(LocalDateTime.now());
        technician.setRole(technicianRole);

        // Save technician first
        Technician savedTechnician = technicianRepository.save(technician);
        log.info("Technician saved with ID: {}", savedTechnician.getId());

        // Process certifications
        List<Certification> certifications = new ArrayList<>();
        if (registerDto.getCertifications() != null && !registerDto.getCertifications().isEmpty()) {
            for (CertificationDto certDto : registerDto.getCertifications()) {
                try {
                    Certification certification = new Certification();
                    certification.setCertificationName(certDto.getCertificationName());
                    certification.setIssuingAuthority(certDto.getIssuingAuthority());

                    // Store file
                    if (certDto.getFile() == null || certDto.getFile().isEmpty()) {
                        throw new RuntimeException("Certification file is required");
                    }

                    String fileName = fileStorageService.storeFile(certDto.getFile());
                    certification.setFilePath(fileName);
                    certification.setOriginalFileName(certDto.getFile().getOriginalFilename());
                    certification.setFileType(certDto.getFile().getContentType());
                    certification.setFileSize(certDto.getFile().getSize());
                    certification.setTechnician(savedTechnician);

                    certifications.add(certificationRepository.save(certification));
                    log.info("Certification saved for technician: {}", savedTechnician.getId());

                } catch (Exception e) {
                    log.error("Error processing certification: {}", e.getMessage());
                    throw new RuntimeException("Failed to process certification: " + e.getMessage());
                }
            }
        }

        savedTechnician.setCertifications(certifications);
        log.info("Technician registration completed successfully for: {}", registerDto.getEmail());

        return mapToResponseDto(savedTechnician);
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("Login attempt for technician: {}", loginDto.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            // Check if technician is approved
            Technician technician = technicianRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

            if (!"ACTIVE".equals(technician.getStatus())) {
                log.warn("Login attempt by non-active technician: {}", loginDto.getEmail());
                throw new UnauthorizedException("Account is not active. Status: " + technician.getStatus());
            }

            log.info("Technician logged in successfully: {}", loginDto.getEmail());
            return token;

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for technician: {}", loginDto.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public TechnicianResponseDto approveTechnician(Long technicianId) {
        log.info("Approving technician with ID: {}", technicianId);

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        technician.setStatus("ACTIVE");
        technician.setApprovalDate(LocalDateTime.now());
        technician.setRejectionReason(null);

        Technician updatedTechnician = technicianRepository.save(technician);
        log.info("Technician approved successfully: {}", technicianId);

        return mapToResponseDto(updatedTechnician);
    }

    @Override
    @Transactional
    public TechnicianResponseDto rejectTechnician(Long technicianId, String reason) {
        log.info("Rejecting technician with ID: {}", technicianId);

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        technician.setStatus("REJECTED");
        technician.setRejectionReason(reason);

        Technician updatedTechnician = technicianRepository.save(technician);
        log.info("Technician rejected: {}", technicianId);

        return mapToResponseDto(updatedTechnician);
    }

    @Override
    public TechnicianResponseDto getTechnicianById(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        return mapToResponseDto(technician);
    }

    @Override
    public List<TechnicianResponseDto> getTechniciansByStatus(String status) {
        List<Technician> technicians = technicianRepository.findByStatus(status);
        return technicians.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TechnicianResponseDto updateTechnicianProfile(Long id, TechnicianRegisterDto updateDto) {
        log.info("Updating technician profile for ID: {}", id);

        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));

        // Update fields
        technician.setFirstName(updateDto.getFirstName());
        technician.setLastName(updateDto.getLastName());
        technician.setPhoneNumber(updateDto.getPhoneNumber());
        technician.setAddress(updateDto.getAddress());
        technician.setSpecialization(updateDto.getSpecialization());
        technician.setYearsOfExperience(updateDto.getYearsOfExperience());

        // Update password only if provided
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            technician.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        Technician updatedTechnician = technicianRepository.save(technician);
        log.info("Technician profile updated successfully for ID: {}", id);

        return mapToResponseDto(updatedTechnician);
    }

    @Override
    @Transactional
    public void deleteTechnician(Long id) {
        log.info("Deleting technician with ID: {}", id);

        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));

        // Delete certification files
        if (technician.getCertifications() != null) {
            for (Certification cert : technician.getCertifications()) {
                fileStorageService.deleteFile(cert.getFilePath());
            }
        }

        technicianRepository.delete(technician);
        log.info("Technician deleted successfully: {}", id);
    }

    private TechnicianResponseDto mapToResponseDto(Technician technician) {
        TechnicianResponseDto dto = new TechnicianResponseDto();
        dto.setId(technician.getId());
        dto.setFirstName(technician.getFirstName());
        dto.setLastName(technician.getLastName());
        dto.setEmail(technician.getEmail());
        dto.setPhoneNumber(technician.getPhoneNumber());
        dto.setAddress(technician.getAddress());
        dto.setSpecialization(technician.getSpecialization());
        dto.setYearsOfExperience(technician.getYearsOfExperience());
        dto.setStatus(technician.getStatus());
        dto.setRegistrationDate(technician.getRegistrationDate());
        dto.setApprovalDate(technician.getApprovalDate());

        if (technician.getCertifications() != null) {
            List<CertificationResponseDto> certDtos = technician.getCertifications().stream()
                    .map(this::mapToCertificationResponseDto)
                    .collect(Collectors.toList());
            dto.setCertifications(certDtos);
        }

        return dto;
    }

    private CertificationResponseDto mapToCertificationResponseDto(Certification certification) {
        CertificationResponseDto dto = new CertificationResponseDto();
        dto.setId(certification.getId());
        dto.setCertificationName(certification.getCertificationName());
        dto.setIssuingAuthority(certification.getIssuingAuthority());
        //dto.setIssueDate(certification.getIssueDate() != null ? certification.getIssueDate().toString() : null);
        //dto.setExpiryDate(certification.getExpiryDate() != null ? certification.getExpiryDate().toString() : null);
        //dto.setCertificateNumber(certification.getCertificateNumber());
        dto.setFileType(certification.getFileType());
        dto.setFileUrl("/uploads/" + certification.getFilePath());
        dto.setOriginalFileName(certification.getOriginalFileName());
        return dto;
    }
}