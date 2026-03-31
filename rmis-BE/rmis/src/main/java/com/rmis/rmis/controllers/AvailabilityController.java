package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.AvailabilityRequestDto;
import com.rmis.rmis.domain.dtos.AvailabilityResponseDto;
import com.rmis.rmis.services.interfaces.AvailabilityService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/technician/availability")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TECHNICIAN')")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * POST /technician/availability
     * Add a new availability slot for the authenticated technician.
     */
    @PostMapping
    public ResponseEntity<?> addAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AvailabilityRequestDto requestDto) {

        log.info("Technician {} adding availability slot", userDetails.getUsername());
        try {
            AvailabilityResponseDto response = availabilityService.addAvailability(
                    userDetails.getUsername(), requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding availability", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * GET /technician/availability
     * Retrieve all future availability slots for the authenticated technician.
     */
    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDto>> getAvailabilities(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Technician {} fetching availability slots", userDetails.getUsername());
        List<AvailabilityResponseDto> slots = availabilityService.getFutureAvailabilities(
                userDetails.getUsername());
        return ResponseEntity.ok(slots);
    }

    /**
     * PUT /technician/availability/{id}
     * Update an existing availability slot.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequestDto requestDto) {

        log.info("Technician {} updating availability slot {}", userDetails.getUsername(), id);
        try {
            AvailabilityResponseDto response = availabilityService.updateAvailability(
                    userDetails.getUsername(), id, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating availability", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * DELETE /technician/availability/{id}
     * Delete an availability slot.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        log.info("Technician {} deleting availability slot {}", userDetails.getUsername(), id);
        try {
            availabilityService.deleteAvailability(userDetails.getUsername(), id);
            return ResponseEntity.ok(Map.of("message", "Availability slot deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting availability", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }
}