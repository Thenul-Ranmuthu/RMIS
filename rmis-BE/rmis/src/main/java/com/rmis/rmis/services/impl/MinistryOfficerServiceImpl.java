package com.rmis.rmis.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.MinistryOfficerService;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import com.rmis.rmis.services.interfaces.AuditLogService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MinistryOfficerServiceImpl implements MinistryOfficerService{

    private QuotaRequestRepository quotaRequestRepository;
    private CompanyRepository companyRepository;
    private MinistryOfficerRepository ministryOfficerRepository;
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public String changeQuotaRequestStatusApprove(UUID id) {
        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No quota found with the ID: " + id));

        if (quotaRequest.getStatus() != QuotaRequestStatus.PENDING) {
            return "Error: Request is already processed (" + quotaRequest.getStatus() + ")";
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MinistryOfficer officer = ministryOfficerRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Officer not found!"));

        quotaRequest.setStatus(QuotaRequestStatus.APPROVED);
        quotaRequest.setReviewedBy(officer);
        quotaRequest.setReviewedAt(LocalDateTime.now());
        quotaRequestRepository.save(quotaRequest);
        
        Company company = companyRepository.findByEmail(quotaRequest.getCompany().getEmail())
            .orElseThrow(() -> new RuntimeException("No Company found!!"));
        company.setQuota(company.getQuota().subtract(quotaRequest.getRequestedQuota()));
        companyRepository.save(company);

        auditLogService.logApproval(officer, quotaRequest);

        return "Status set to APPROVED";
    }

    @Override
    @Transactional
    public String changeQuotaRequestStatusReject(UUID id, String reason) {
        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No quota found with the ID: " + id));

        if (quotaRequest.getStatus() != QuotaRequestStatus.PENDING) {
            return "Error: Request is already processed (" + quotaRequest.getStatus() + ")";
        }
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        MinistryOfficer officer = ministryOfficerRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Officer not found!"));

        quotaRequest.setStatus(QuotaRequestStatus.REJECTED);
        quotaRequest.setReviewedBy(officer);
        quotaRequest.setReviewedAt(LocalDateTime.now());
        quotaRequest.setRejectionReason(reason);
        quotaRequestRepository.save(quotaRequest);

        auditLogService.logRejection(officer, quotaRequest, reason);

        return "Status set to REJECTED";
    }

}
