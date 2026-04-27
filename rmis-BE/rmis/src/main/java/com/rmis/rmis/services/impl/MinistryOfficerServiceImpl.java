package com.rmis.rmis.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.repositories.AnnualQuotaDistributionRepository;
import com.rmis.rmis.repositories.QuotaRequestAnalyticsRepository;
import com.rmis.rmis.services.interfaces.AuditLogService;
import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.MinistryOfficerService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MinistryOfficerServiceImpl implements MinistryOfficerService{

    private QuotaRequestRepository quotaRequestRepository;
    private CompanyRepository companyRepository;
    private final AuditLogService auditLogService;
    private final AnnualQuotaDistributionRepository annualQuotaRepo;
    private final QuotaRequestAnalyticsRepository requestRepo;

    @Override
    public String changeQuotaRequestStatusApprove(UUID id, MinistryOfficer officer) {
        if(!quotaRequestRepository.existsById(id)){
            return "No quota found with the ID: " + id;
        }

        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));

        Company company = companyRepository.findByEmail(quotaRequest.getCompany().getEmail())
                .orElseThrow(() -> new RuntimeException("No Company found!!"));


        System.out.println("remaining: " + company.getRemainingQuota().subtract(quotaRequest.getRequestedQuota()).signum());
        if(company.getRemainingQuota().subtract(quotaRequest.getRequestedQuota()).signum() == 1 || company.getRemainingQuota().subtract(quotaRequest.getRequestedQuota()).signum() == 0){
            company.setRemainingQuota(company.getRemainingQuota().subtract(quotaRequest.getRequestedQuota()));
            companyRepository.save(company);

            quotaRequest.setStatus(QuotaRequestStatus.APPROVED);
            quotaRequest.setReviewedBy(officer);
            quotaRequest.setReviewedAt(LocalDateTime.now());
            quotaRequestRepository.save(quotaRequest);
            auditLogService.logApproval(officer, quotaRequest);
            quotaRequest.setApprovedAmount(quotaRequest.getRequestedQuota());

            return "Status set to APPROVED";
        }else{
            return "Insufficient quota remaining. Failed to approve quota.";
        }
        
    }

    @Override
    public String changeQuotaRequestStatusReject(UUID id, MinistryOfficer officer) {
        if(!quotaRequestRepository.existsById(id)){
            return "No quota found with the ID: " + id;
        }

        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));
        
        quotaRequest.setStatus(QuotaRequestStatus.REJECTED);
        quotaRequest.setReviewedBy(officer);
        quotaRequest.setReviewedAt(LocalDateTime.now());
        
        quotaRequestRepository.save(quotaRequest);
        auditLogService.logRejection(officer, quotaRequest);

        return "Status set to REJECTED";
    }

}
