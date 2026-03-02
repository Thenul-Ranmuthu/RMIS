package com.rmis.rmis.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterResponseDto;
import com.rmis.rmis.domain.entities.Certification;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.repositories.CertificationRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TechnicianAuthServiceImpl implements TechnicianAuthService{

    private final TechnicianRepository technicianRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CertificationRepository certificationRepository;
    private final AuthenticationProvider authenticationProvider;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final JwtTokenProvider jwtTokenProvider;

    

    public TechnicianAuthServiceImpl(
        TechnicianRepository technicianRepository,
        PasswordEncoder passwordEncoder,
        RoleRepository roleRepository,
        CertificationRepository certificationRepository,
        FileStorageServiceImpl fileStorageServiceImpl,
        @Qualifier("technicianAuthenticationProvider")
        AuthenticationProvider authenticationProvider,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.technicianRepository = technicianRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.certificationRepository = certificationRepository;
        this.fileStorageServiceImpl = fileStorageServiceImpl;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public TechnicianRegisterResponseDto register(TechnicianRegisterDto technicianRegisterDto) {
        // 1. Check if email already exists
        if (technicianRepository.existsByEmail(technicianRegisterDto.getEmail())) {
            throw new RegisterUserAlreadyExistsException("Email already exists");
        }

        // 2. Validate certification files and names match
        if (technicianRegisterDto.getCertificationFiles().size() != technicianRegisterDto.getCertificationNames().size()) {
            throw new IllegalArgumentException("Number of certification files must match number of certification names");
        }

        Technician technician = new Technician();
        technician.setFirstName(technicianRegisterDto.getFirstName());
        technician.setLastName(technicianRegisterDto.getLastName());
        technician.setEmail(technicianRegisterDto.getEmail());
        technician.setPassword(passwordEncoder.encode(technicianRegisterDto.getPassword()));
        technician.setPhoneNumber(technicianRegisterDto.getPhoneNumber());
        technician.setSpecialization(technicianRegisterDto.getSpecialization());
        technician.setStatus("PENDING");
        technician.setRole(roleRepository.findByName("ROLE_TECHNITIAN"));

        Technician savedTechnician = technicianRepository.save(technician);
        log.info("Technician saved with ID: {}", savedTechnician.getId());

        List<MultipartFile> files = technicianRegisterDto.getCertificationFiles();
        List<String> names = technicianRegisterDto.getCertificationNames();


        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // Store file on disk under uploads/certifications/{technicianId}/
            String filePath = fileStorageServiceImpl.storeCertification(file, savedTechnician.getId());

            // Save certification record in DB
            Certification certification = new Certification();
            certification.setName(names.get(i));
            certification.setFileName(file.getOriginalFilename());
            certification.setFilePath(filePath);
            certification.setFileType(file.getContentType());
            certification.setTechnician(savedTechnician);


            certificationRepository.save(certification);
            log.info("Certification saved: {}", names.get(i));
        }
        TechnicianRegisterResponseDto technicianRegisterResponseDto = new TechnicianRegisterResponseDto();
        technicianRegisterResponseDto.setMsg("Set to pending");
        return technicianRegisterResponseDto;
    }

    @Override
    public Technician getPendingTechnicians(LoginDto loginDto) {
        Technician technician = technicianRepository.findByEmail(loginDto.getEmail())
            .orElseThrow(() -> new RuntimeException("Technician not verified: " + loginDto.getEmail()));
        if(technician.getStatus().equalsIgnoreCase("VERIFIED")){
            return technician;
        }else{
            return null;
        }
    }

    @Override
    public String login(LoginDto loginDto) {
        if(technicianRepository.existsByEmail(loginDto.getEmail())){
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), 
                loginDto.getPassword()
            ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "TECHNICIAN");
        
        return token;
        }else{
            throw new UnregisteredUserException("User with this email does not exist or password is incorrect.");
        }
    }

}
