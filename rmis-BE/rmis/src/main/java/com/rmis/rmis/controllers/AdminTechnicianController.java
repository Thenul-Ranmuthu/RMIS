package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.TechnicianResponseDto;
import com.rmis.rmis.domain.enums.SkillLevel;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/technicians")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTechnicianController {

    private final TechnicianAuthService technicianAuthService;

    @GetMapping("/pending")
    public ResponseEntity<List<TechnicianResponseDto>> getPendingTechnicians() {
        log.info("Admin fetching pending technicians");
        return new ResponseEntity<>(technicianAuthService.getTechniciansByStatus("PENDING"), HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TechnicianResponseDto>> getActiveTechnicians() {
        log.info("Admin fetching active technicians");
        return new ResponseEntity<>(technicianAuthService.getTechniciansByStatus("ACTIVE"), HttpStatus.OK);
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<TechnicianResponseDto>> getRejectedTechnicians() {
        log.info("Admin fetching rejected technicians");
        return new ResponseEntity<>(technicianAuthService.getTechniciansByStatus("REJECTED"), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTechnicianById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(technicianAuthService.getTechnicianById(id), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveTechnician(
            @PathVariable Long id,
            @RequestParam SkillLevel skillLevel) {
        log.info("Admin approving technician: {} with skillLevel: {}", id, skillLevel);
        try {
            TechnicianResponseDto response = technicianAuthService.approveTechnician(id, skillLevel);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Technician approved successfully");
            result.put("technician", response);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error approving technician", e);
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectTechnician(@PathVariable Long id,
                                              @RequestParam String reason) {
        log.info("Admin rejecting technician: {}", id);
        if (reason == null || reason.trim().isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Rejection reason is required"), HttpStatus.BAD_REQUEST);
        }
        try {
            TechnicianResponseDto response = technicianAuthService.rejectTechnician(id, reason);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Technician rejected successfully");
            result.put("technician", response);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error rejecting technician", e);
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnician(@PathVariable Long id) {
        log.info("Admin deleting technician: {}", id);
        try {
            technicianAuthService.deleteTechnician(id);
            return new ResponseEntity<>(Map.of("message", "Technician deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error deleting technician", e);
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}