package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.AvailabilityResponseDto;
import com.rmis.rmis.domain.entities.Availability;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.AvailabilityRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/public/technicians")
@AllArgsConstructor
public class PublicAvailabilityController {

    private final AvailabilityRepository availabilityRepository;
    private final TechnicianRepository technicianRepository;

    @GetMapping("/{technicianId}/availability")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable Long technicianId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Public query: available slots for technician {} on {}", technicianId, date);
        try {
            Technician technician = technicianRepository.findById(technicianId)
                    .filter(t -> "ACTIVE".equals(t.getStatus()))
                    .orElse(null);

            if (technician == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Technician not found or not available"));
            }

            List<Availability> availabilities;
            if (date != null) {
                availabilities = availabilityRepository
                        .findByTechnicianIdAndDateAndStatusOrderByStartTimeAsc(
                                technicianId, date, "AVAILABLE");
            } else {
                availabilities = availabilityRepository
                        .findByTechnicianIdAndDateGreaterThanEqualAndStatusOrderByDateAscStartTimeAsc(
                                technicianId, LocalDate.now(), "AVAILABLE");
            }

            List<AvailabilityResponseDto> slots = availabilities.stream()
                    .map(a -> toDto(a, technician))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            log.error("Error fetching available slots for technician {}", technicianId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch availability"));
        }
    }

    private AvailabilityResponseDto toDto(Availability a, Technician technician) {
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setId(a.getId());
        dto.setTechnicianId(technician.getId());
        dto.setTechnicianName(technician.getFirstName() + " " + technician.getLastName());
        dto.setDate(a.getDate());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus());
        return dto;
    }
}