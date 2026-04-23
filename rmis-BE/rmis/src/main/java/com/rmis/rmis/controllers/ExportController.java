package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.enums.ExportFormat;
import com.rmis.rmis.exceptions.ExportException;
import com.rmis.rmis.services.impl.export.QuotaExportOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final QuotaExportOrchestrator orchestrator;
    @GetMapping("/quota")
    @PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY_OFFICER')")
    public ResponseEntity<byte[]> exportQuotaReport(
            @RequestParam(defaultValue = "CSV") ExportFormat format)
            throws ExportException {

        QuotaExportOrchestrator.ExportResult result = orchestrator.export(format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.contentType()));
        headers.setContentDispositionFormData("attachment", result.filename());
        headers.setContentLength(result.bytes().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.bytes());
    }
}
