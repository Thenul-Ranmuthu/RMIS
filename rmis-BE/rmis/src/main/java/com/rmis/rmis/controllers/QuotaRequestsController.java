package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestDetailDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ministry/quota-requests")
@PreAuthorize("hasRole('MINISTRY_OFFICER')")
@RequiredArgsConstructor
public class QuotaRequestsController {
    private final QuotaRequestService quotaRequestService;

    @GetMapping
    public ResponseEntity<List<QuotaRequestResponseDto>> getAllRequests() {
        if (quotaRequestService.getAllRequests().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        System.out.println(quotaRequestService.getAllRequests());
        return ResponseEntity.ok(quotaRequestService.getAllRequests());
    }

    @GetMapping("/paginated")
    public ResponseEntity<PagedResponseDto<QuotaRequestResponseDto>> getQuotaRequestsPaginated(
            @RequestParam(defaultValue = "1")  int page,    // defaults to page 1
            @RequestParam(defaultValue = "5")  int limit    // defaults to 5 per page
    ) {
        return ResponseEntity.ok(
                quotaRequestService.getQuotaRequestsPaginated(page, limit)
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<PagedResponseDto<QuotaRequestResponseDto>> getQuotaRequestsFiltered(
            @RequestParam(required = false)
            QuotaRequestStatus status,

            @RequestParam(required = false, name = "company_name")
            String companyName,

            @RequestParam(required = false, name = "submission_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate submissionDate,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(
                quotaRequestService.getFilteredRequests(
                        status,
                        companyName,
                        submissionDate,
                        page,
                        limit
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuotaRequestDetailDto> getRequestById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(quotaRequestService.getRequestById(id));
    }
}
