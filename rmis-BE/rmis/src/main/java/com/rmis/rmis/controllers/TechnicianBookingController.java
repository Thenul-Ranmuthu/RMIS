package com.rmis.rmis.controllers;
import com.rmis.rmis.domain.dtos.TechnicianBookingResponseDto;
import com.rmis.rmis.services.interfaces.TechnicianBookingService;
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
@RequestMapping("/technician/bookings")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianBookingController {
    private final TechnicianBookingService technicianBookingService;
    @GetMapping
    public ResponseEntity<?> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status) {
        log.info("Technician {} fetching bookings, status filter: {}",
                userDetails.getUsername(), status);
        try {
            List<TechnicianBookingResponseDto> bookings = (status != null &&
                    !status.isBlank())
                    ? technicianBookingService.getMyBookingsByStatus(
                    userDetails.getUsername(), status)
                    : technicianBookingService.getMyBookings(
                    userDetails.getUsername());
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching bookings for technician {}",
                    userDetails.getUsername(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        log.info("Technician {} fetching booking detail for ticket {}",
                userDetails.getUsername(), id);
        try {
            TechnicianBookingResponseDto detail =
                    technicianBookingService.getBookingDetail(
                            userDetails.getUsername(), id);
            return ResponseEntity.ok(detail);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Booking not found"));
        }
    }
}