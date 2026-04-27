package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.ServiceRatingResponseDto;
import com.rmis.rmis.domain.dtos.TechnicianResponseDto;
import com.rmis.rmis.domain.enums.SkillLevel;
import com.rmis.rmis.services.interfaces.ServiceTicketService;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/public")
@AllArgsConstructor
public class PublicDirectoryController {

    private final TechnicianAuthService technicianAuthService;
    private final ServiceTicketService serviceTicketService;

    @GetMapping("/technicians/active")
    public ResponseEntity<?> getActiveTechnicians() {
        log.info("Public user fetching active technicians");
        try {
            List<TechnicianResponseDto> technicians =
                    technicianAuthService.getTechniciansByStatus("ACTIVE");
            return new ResponseEntity<>(technicians, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching active technicians", e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to fetch technicians: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/technicians/search")
    public ResponseEntity<?> searchAvailableTechnicians(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) SkillLevel skillLevel) {

        log.info("Searching available technicians for date: {}, skillLevel: {}", date, skillLevel);
        try {
            List<TechnicianResponseDto> technicians =
                    technicianAuthService.searchAvailableTechnicians(date, skillLevel);
            return new ResponseEntity<>(technicians, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error searching available technicians", e);
            return new ResponseEntity<>(
                    Map.of("error", "Failed to search technicians: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/technician/{id}")
    public ResponseEntity<?> getTechnicianPublicProfile(@PathVariable Long id) {
        log.info("Public user fetching technician profile: {}", id);
        try {
            TechnicianResponseDto technician = technicianAuthService.getTechnicianById(id);
            if (!"ACTIVE".equals(technician.getStatus())) {
                return new ResponseEntity<>(Map.of("error", "Technician not available"), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(technician, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching technician profile", e);
            return new ResponseEntity<>(Map.of("error", "Technician not found"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/technicians/{id}/feedbacks")
    public ResponseEntity<?> getTechnicianFeedbacks(@PathVariable Long id) {
        log.info("Public user fetching feedbacks for technician: {}", id);
        try {
            List<ServiceRatingResponseDto> feedbacks = serviceTicketService.getTechnicianFeedbacks(id);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            log.error("Error fetching feedbacks for technician: {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch feedbacks"));
        }
    }
}