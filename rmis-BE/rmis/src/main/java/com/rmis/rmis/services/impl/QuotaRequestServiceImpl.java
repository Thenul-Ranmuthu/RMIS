package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestDetailDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.domain.enums.QuotaRequestStatus;
import com.rmis.rmis.exceptions.QuotaRequestNotFoundException;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.QuotaRequestsSpecification;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuotaRequestServiceImpl implements QuotaRequestService {
    private final QuotaRequestRepository quotaRequestRepository;

    public QuotaRequestServiceImpl(QuotaRequestRepository quotaRequestRepository) {
        this.quotaRequestRepository = quotaRequestRepository;
    }

    @Override
    public List<QuotaRequestResponseDto> getAllRequests(){
        return quotaRequestRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PagedResponseDto<QuotaRequestResponseDto> getQuotaRequestsPaginated(
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.DESC, "submissionDate")     
        );

        // Execute paginated query — no filters, just page + sort
        Page<QuotaRequest> resultPage = quotaRequestRepository.findAll(pageable);

        // Map each entity to DTO
        List<QuotaRequestResponseDto> data = resultPage.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // Wrap in paginated response with metadata
        return PagedResponseDto.<QuotaRequestResponseDto>builder()
                .data(data)
                .totalRecords(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(page)                                  // return 1-based page to client
                .build();
    }

    public PagedResponseDto<QuotaRequestResponseDto> getFilteredRequests(QuotaRequestStatus status,
                                                                         String companyName,
                                                                         LocalDate submissionDate,
                                                                         int page, int limit) {
        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.DESC, "submissionDate")
        );

        Specification<QuotaRequest> spec = QuotaRequestsSpecification.withFilters(status, companyName, submissionDate);

        Page<QuotaRequest> resultPage = quotaRequestRepository.findAll(spec, pageable);

        // Map each entity to DTO
        List<QuotaRequestResponseDto> data = resultPage.getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // Wrap in paginated response
        return PagedResponseDto.<QuotaRequestResponseDto>builder()
                .data(data)
                .totalRecords(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(page)                                  // return 1-based page to client
                .build();
    }


    private QuotaRequestResponseDto toDto(QuotaRequest entity) {
        return QuotaRequestResponseDto.builder()
                .id(entity.getRequestId())
                .requestId(formatRequestId(entity.getRequestNumber()))
                .companyName(entity.getCompanyName())
                .requestedQuota(entity.getRequestedQuota())
                .submissionDate(entity.getSubmissionDate())
                .status(entity.getStatus())
                .build();
    }

    private String formatRequestId(Long requestNumber) {
        return String.format("REQ-%04d", requestNumber);
    }

    public QuotaRequestDetailDto getRequestById(UUID requestId) {
        QuotaRequest entity = quotaRequestRepository.findById(requestId)
                .orElseThrow(() -> new QuotaRequestNotFoundException("Quota request not found: " + requestId));

        return toDetailDto(entity);
    }

    private QuotaRequestDetailDto toDetailDto(QuotaRequest entity) {
        return QuotaRequestDetailDto.builder()
                .id(entity.getRequestId())
                .requestId(formatRequestId(entity.getRequestNumber()))
                .companyName(entity.getCompanyName())
                .companyEmail(entity.getCompany().getEmail())
                .companyIdentifier(entity.getCompany().getCompanyid())
                .requestedQuota(entity.getRequestedQuota())
                .submissionDate(entity.getSubmissionDate())
                .status(entity.getStatus())
                .reviewedBy(entity.getReviewedBy() != null
                        ? entity.getReviewedBy().getName()
                        : null)
                .reviewedAt(entity.getReviewedAt())
                .build();
    }
}
