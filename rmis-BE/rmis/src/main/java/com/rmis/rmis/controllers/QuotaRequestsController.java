package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ministry/quota-requests")
@RequiredArgsConstructor
public class QuotaRequestsController {
    private final QuotaRequestService quotaRequestService;

    @GetMapping
    @PreAuthorize("hasRole('MINISTRY_OFFICER')")
    public ResponseEntity<List<QuotaRequestResponseDto>> getAllRequests() {
        if (quotaRequestService.getAllRequests().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        System.out.println(quotaRequestService.getAllRequests());
        return ResponseEntity.ok(quotaRequestService.getAllRequests());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('MINISTRY_OFFICER')")
    public ResponseEntity<PagedResponseDto<QuotaRequestResponseDto>> getQuotaRequestsPaginated(
            @RequestParam(defaultValue = "1")  int page,    // defaults to page 1
            @RequestParam(defaultValue = "5")  int limit    // defaults to 5 per page
    ) {
        return ResponseEntity.ok(
                quotaRequestService.getQuotaRequestsPaginated(page, limit)
        );
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('MINISTRY_OFFICER')")
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
}
