package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.AuditLogResponseDto;
import com.rmis.rmis.services.interfaces.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    // GET /admin/audit-logs?from=2026-03-01&to=2026-03-19
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogs(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to

    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogs(from, to));
    }
}
