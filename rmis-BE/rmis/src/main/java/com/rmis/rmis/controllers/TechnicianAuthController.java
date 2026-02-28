package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.*;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.FileStorageException;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/technician")
@AllArgsConstructor
public class TechnicianAuthController {

    private final TechnicianAuthService technicianAuthService;
    private final TechnicianRepository technicianRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(@Valid @ModelAttribute TechnicianRegisterDto registerDto) {
        log.info("Received technician registration request for email: {}", registerDto.getEmail());

        try {
            TechnicianResponseDto response = technicianAuthService.register(registerDto);
            log.info("Technician registered successfully with ID: {}", response.getId());

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Registration successful. Pending admin approval.");
            successResponse.put("technician", response);

            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);

        } catch (RegisterUserAlreadyExistsException e) {
            log.warn("Registration failed: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);

        } catch (FileStorageException e) {
            log.warn("File upload error: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "File upload error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            log.error("Registration failed unexpectedly", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        log.info("Received login request for technician: {}", loginDto.getEmail());

        try {
            String token = technicianAuthService.login(loginDto);

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setAccessToken(token);
            jwtAuthResponse.setTokenType("Bearer");
            jwtAuthResponse.setRole("ROLE_TECHNICIAN");
            jwtAuthResponse.setEmail(loginDto.getEmail());

            log.info("Login successful for technician: {}", loginDto.getEmail());
            return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.warn("Login failed for technician: {}", loginDto.getEmail());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("Fetching profile for technician: {}", email);

            Technician technician = technicianRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

            TechnicianResponseDto response = technicianAuthService.getTechnicianById(technician.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.warn("Profile fetch failed: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @Valid @RequestBody TechnicianRegisterDto updateDto) {
        try {
            String email = authentication.getName();
            log.info("Updating profile for technician: {}", email);

            Technician technician = technicianRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

            TechnicianResponseDto response = technicianAuthService.updateTechnicianProfile(
                    technician.getId(), updateDto);

            log.info("Profile updated successfully for technician: {}", email);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.warn("Profile update failed: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}