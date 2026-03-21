package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.QuotaRequestsSpecification;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuotaRequestServiceImpl implements QuotaRequestService {
    private final QuotaRequestRepository quotaRequestRepository;
    private final CompanyRepository companyRepository;

    public QuotaRequestServiceImpl(QuotaRequestRepository quotaRequestRepository, CompanyRepository companyRepository) {
        this.quotaRequestRepository = quotaRequestRepository;
        this.companyRepository = companyRepository;
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
                // .requestId(formatRequestId(entity.getRequestNumber()))
                .requestId(entity.getRequestId().toString())
                .companyName(entity.getCompanyName())
                .requestedQuota(entity.getRequestedQuota())
                .submissionDate(entity.getSubmissionDate())
                .status(entity.getStatus())
                .build();
    }

    // private String formatRequestId(Long requestNumber) {
    //     return String.format("REQ-%04d", requestNumber);
    // }

    @Override
    public String addQuotaRequest(QuotaRequestHeaderDto quotaRequestHeaderDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        if(quotaRequestHeaderDto.getRequestedQuota().compareTo(company.getQuota()) > 0){
            return "Error: Insuffitient quota balance!!";
        }
        

        QuotaRequest entity = new QuotaRequest();

        entity.setCompany(company);
        entity.setRequestedQuota(quotaRequestHeaderDto.getRequestedQuota());
        entity.setCompanyName(company.getName());
        entity.setUpdatedAt(LocalDateTime.now());

        long nextNumber = quotaRequestRepository.count() + 1;
        entity.setRequestNumber(nextNumber);
        

        quotaRequestRepository.save(entity);
        return "Quota saved succefully!!";
    }
}
