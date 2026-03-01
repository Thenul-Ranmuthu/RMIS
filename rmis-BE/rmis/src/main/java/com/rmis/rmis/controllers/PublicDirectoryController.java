package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.TechnicianResponseDto;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/public")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PublicDirectoryController {

    private final TechnicianAuthService technicianAuthService;

    @GetMapping("/technicians/active")
    public ResponseEntity<?> getActiveTechnicians() {
        log.info("Public user fetching active technicians");

        try {
            List<TechnicianResponseDto> activeTechnicians =
                    technicianAuthService.getTechniciansByStatus("ACTIVE");

            if (activeTechnicians.isEmpty()) {
                return new ResponseEntity<>(activeTechnicians, HttpStatus.OK);
            }

            return new ResponseEntity<>(activeTechnicians, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching active technicians", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch technicians: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/technician/{id}")
    public ResponseEntity<?> getTechnicianPublicProfile(@PathVariable Long id) {
        log.info("Public user fetching technician profile: {}", id);

        try {
            TechnicianResponseDto technician = technicianAuthService.getTechnicianById(id);

            // Only return if technician is ACTIVE
            if (!"ACTIVE".equals(technician.getStatus())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Technician not available");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(technician, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching technician profile", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Technician not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}