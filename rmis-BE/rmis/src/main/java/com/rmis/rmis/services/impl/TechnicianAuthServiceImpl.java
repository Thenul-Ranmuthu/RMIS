package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.*;
import com.rmis.rmis.domain.entities.Certification;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.repositories.CertificationRepository;
import com.rmis.rmis.repositories.RoleRepository;
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

    public TechnicianAuthServiceImpl(
            TechnicianRepository technicianRepository,
            CertificationRepository certificationRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            FileStorageServiceImpl fileStorageServiceImpl,
            @Qualifier("technicianAuthenticationProvider")
            AuthenticationProvider authenticationProvider,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.technicianRepository = technicianRepository;
        this.certificationRepository = certificationRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageServiceImpl = fileStorageServiceImpl;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
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
    public TechnicianResponseDto approveTechnician(Long technicianId) {
        log.info("Approving technician with ID: {}", technicianId);
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));
        technician.setStatus("ACTIVE");
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
    public TechnicianResponseDto getTechnicianById(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        return mapToResponseDto(technician);
    }

    @Override
    public List<TechnicianResponseDto> getTechniciansByStatus(String status) {
        return technicianRepository.findByStatus(status).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTechnician(Long id) {
        log.info("Deleting technician with ID: {}", id);
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
        if (technician.getCertifications() != null) {
            for (Certification cert : technician.getCertifications()) {
                fileStorageServiceImpl.deleteFile(cert.getFilePath());
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
                    .map(cert -> {
                        CertificationResponseDto c = new CertificationResponseDto();
                        c.setId(cert.getId());
                        c.setCertificationName(cert.getCertificationName());
                        c.setFileType(cert.getFileType());
                        c.setOriginalFileName(cert.getOriginalFileName());
                        c.setFileUrl("/uploads/" + cert.getFilePath());
                        return c;
                    })
                    .collect(Collectors.toList());
            dto.setCertifications(certDtos);
        }
        return dto;
    }
}