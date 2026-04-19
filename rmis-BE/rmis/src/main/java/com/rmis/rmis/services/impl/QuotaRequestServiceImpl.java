package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.domain.dtos.QuotaRequestDetailDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.mappers.Mapper;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class QuotaRequestServiceImpl implements QuotaRequestService {

    private final QuotaRequestRepository quotaRequestRepository;
    private final CompanyRepository companyRepository;
    private final Mapper<QuotaRequest, QuotaRequestResponseDto> quotaRequestResponseMapper;
    private final Mapper<QuotaRequest, QuotaRequestDetailDto> quotaRequestDetailMapper;
    private final EmailService emailService;

    @Override
    public List<QuotaRequestResponseDto> getAllRequests() {
        List<QuotaRequest> entities = (List<QuotaRequest>) quotaRequestRepository.findAll();
        return entities.stream()
                .map(quotaRequestResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponseDto<QuotaRequestResponseDto> getQuotaRequestsPaginated(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<QuotaRequest> quotaRequestPage = quotaRequestRepository.findAll(pageable);

        List<QuotaRequestResponseDto> content = quotaRequestPage.getContent().stream()
                .map(quotaRequestResponseMapper::mapTo)
                .collect(Collectors.toList());

        return new PagedResponseDto<>(
                content,
                quotaRequestPage.getNumber() + 1,
                quotaRequestPage.getSize(),
                quotaRequestPage.getTotalElements(),
                quotaRequestPage.getTotalPages(),
                quotaRequestPage.isLast()
        );
    }

    @Override
    public PagedResponseDto<QuotaRequestResponseDto> getFilteredRequests(
            QuotaRequestStatus status,
            String companyName,
            LocalDate submissionDate,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<QuotaRequest> quotaRequestPage = quotaRequestRepository.findFilteredRequests(
                status,
                companyName,
                submissionDate,
                pageable
        );

        List<QuotaRequestResponseDto> content = quotaRequestPage.getContent().stream()
                .map(quotaRequestResponseMapper::mapTo)
                .collect(Collectors.toList());

        return new PagedResponseDto<>(
                content,
                quotaRequestPage.getNumber() + 1,
                quotaRequestPage.getSize(),
                quotaRequestPage.getTotalElements(),
                quotaRequestPage.getTotalPages(),
                quotaRequestPage.isLast()
        );
    }

    @Override
    public QuotaRequestDetailDto getRequestById(UUID id) {
        QuotaRequest entity = quotaRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quota Request not found with ID: " + id));
        return quotaRequestDetailMapper.mapTo(entity);
    }

    @Override
    @Transactional
    public String addQuotaRequest(QuotaRequestAddQuotaDto quotaRequestHeaderDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        // Logic Check: Prevent negative or zero quota requests
        if (quotaRequestHeaderDto.getRequestedQuota() == null || 
            quotaRequestHeaderDto.getRequestedQuota().compareTo(BigDecimal.ZERO) <= 0) {
            return "Error: Requested quota must be greater than zero!!";
        }

        boolean hasPending = quotaRequestRepository.existsByCompanyAndStatus(
                company, QuotaRequestStatus.PENDING);
        if (hasPending) {
            return "Error: You already have a pending quota request!!";
        }

        QuotaRequest entity = new QuotaRequest();
        entity.setCompanyName(company.getName());
        entity.setCompanyEmail(company.getEmail());
        entity.setRequestedQuota(quotaRequestHeaderDto.getRequestedQuota());
        entity.setStatus(QuotaRequestStatus.PENDING);
        entity.setSubmissionDate(LocalDate.now());
        entity.setCompany(company);

        // Generate request number (simple count-based for now)
        long nextNumber = quotaRequestRepository.count() + 1;
        entity.setRequestNumber("QR-" + String.format("%05d", nextNumber));

        quotaRequestRepository.save(entity);

        // Safety: ensure DTO has the correct email for notification
        quotaRequestHeaderDto.setCompanyEmail(company.getEmail());
        
        // Notify Ministry Officers
        emailService.sendNotificationNewRequestSubmission(quotaRequestHeaderDto);

        return "Quota request submitted successfully. Request Number: " + entity.getRequestNumber();
    }
}
