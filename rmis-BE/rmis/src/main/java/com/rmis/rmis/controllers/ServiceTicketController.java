package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;
import com.rmis.rmis.services.interfaces.ServiceTicketService;
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

/**
 * Endpoints for raising and viewing service tickets (booking requests).
 *
 * POST  /api/service-tickets/user          → raise ticket (public user)
 * POST  /api/service-tickets/company       → raise ticket (company)
 * GET   /api/service-tickets/user/my       → my tickets (public user)
 * GET   /api/service-tickets/company/my    → my tickets (company)
 * GET   /api/service-tickets/{id}          → single ticket (any authenticated role)
 */
@RestController
@RequestMapping("/api/service-tickets")
@AllArgsConstructor
@Slf4j
public class ServiceTicketController {

    private final ServiceTicketService serviceTicketService;

    // ── Public-user endpoints ─────────────────────────────────────────────────

    @PostMapping("/user")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> raiseTicketAsUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ServiceTicketRequestDto requestDto) {

        log.info("Public user {} raising service ticket", userDetails.getUsername());
        try {
            ServiceTicketResponseDto response =
                    serviceTicketService.raiseTicketAsPublicUser(
                            userDetails.getUsername(), requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // Slot already booked – 409 Conflict (Scenario 2)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error raising service ticket", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/user/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ServiceTicketResponseDto>> getMyTicketsAsUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                serviceTicketService.getMyTicketsAsPublicUser(userDetails.getUsername()));
    }

    // ── Company endpoints ─────────────────────────────────────────────────────

    @PostMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> raiseTicketAsCompany(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ServiceTicketRequestDto requestDto) {

        log.info("Company {} raising service ticket", userDetails.getUsername());
        try {
            ServiceTicketResponseDto response =
                    serviceTicketService.raiseTicketAsCompany(
                            userDetails.getUsername(), requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error raising service ticket", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/company/my")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<ServiceTicketResponseDto>> getMyTicketsAsCompany(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                serviceTicketService.getMyTicketsAsCompany(userDetails.getUsername()));
    }

    // ── Shared endpoint ───────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','COMPANY','TECHNICIAN','ADMIN')")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(serviceTicketService.getTicketById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Service ticket not found"));
        }
    }
}