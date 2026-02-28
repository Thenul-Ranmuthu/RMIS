package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.*;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;
import jakarta.validation.Valid;
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
    private final TechnicianRepository technicianRepository;

    @GetMapping("/pending")
    public ResponseEntity<List<TechnicianResponseDto>> getPendingTechnicians() {
        log.info("Admin fetching pending technicians");
        List<TechnicianResponseDto> pendingTechnicians = technicianAuthService.getTechniciansByStatus("PENDING_APPROVAL");
        return new ResponseEntity<>(pendingTechnicians, HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TechnicianResponseDto>> getActiveTechnicians() {
        log.info("Admin fetching active technicians");
        List<TechnicianResponseDto> activeTechnicians = technicianAuthService.getTechniciansByStatus("ACTIVE");
        return new ResponseEntity<>(activeTechnicians, HttpStatus.OK);
    }

    @GetMapping("/rejected")
    public ResponseEntity<List<TechnicianResponseDto>> getRejectedTechnicians() {
        log.info("Admin fetching rejected technicians");
        List<TechnicianResponseDto> rejectedTechnicians = technicianAuthService.getTechniciansByStatus("REJECTED");
        return new ResponseEntity<>(rejectedTechnicians, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTechnicianById(@PathVariable Long id) {
        try {
            TechnicianResponseDto technician = technicianAuthService.getTechnicianById(id);
            return new ResponseEntity<>(technician, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveTechnician(@Valid @RequestBody TechnicianApprovalDto approvalDto) {
        log.info("Admin processing technician approval: {}", approvalDto.getTechnicianId());

        try {
            TechnicianResponseDto response;

            if ("APPROVE".equalsIgnoreCase(approvalDto.getAction())) {
                response = technicianAuthService.approveTechnician(approvalDto.getTechnicianId());
                log.info("Technician approved: {}", approvalDto.getTechnicianId());

            } else if ("REJECT".equalsIgnoreCase(approvalDto.getAction())) {
                if (approvalDto.getRejectionReason() == null || approvalDto.getRejectionReason().trim().isEmpty()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Rejection reason is required");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
                response = technicianAuthService.rejectTechnician(
                        approvalDto.getTechnicianId(),
                        approvalDto.getRejectionReason()
                );
                log.info("Technician rejected: {}", approvalDto.getTechnicianId());

            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid action. Use APPROVE or REJECT");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Technician " + approvalDto.getAction().toLowerCase() + "d successfully");
            successResponse.put("technician", response);

            return new ResponseEntity<>(successResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error processing technician approval", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnician(@PathVariable Long id) {
        log.info("Admin deleting technician: {}", id);

        try {
            technicianAuthService.deleteTechnician(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Technician deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error deleting technician", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}