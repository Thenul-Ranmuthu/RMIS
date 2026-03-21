package com.rmis.rmis.services.impl;

import java.util.UUID;

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

    @Override
    public String changeQuotaRequestStatusApprove(UUID id) {
        if(!quotaRequestRepository.existsById(id)){
            return "No quota found with the ID: " + id;
        }

        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));

        quotaRequest.setStatus(QuotaRequestStatus.APPROVED);
        
        quotaRequestRepository.save(quotaRequest);
        
        Company company = companyRepository.findByEmail(quotaRequest.getCompany().getEmail())
            .orElseThrow(() -> new RuntimeException("No Company found!!"));

        company.setQuota(company.getQuota().subtract(quotaRequest.getRequestedQuota()));

        companyRepository.save(company);

        return "Status set to APPROVED";
    }

    @Override
    public String changeQuotaRequestStatusReject(UUID id) {
        if(!quotaRequestRepository.existsById(id)){
            return "No quota found with the ID: " + id;
        }

        QuotaRequest quotaRequest = quotaRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No Quota request!!"));
        
        quotaRequest.setStatus(QuotaRequestStatus.REJECTED);
        
        quotaRequestRepository.save(quotaRequest);

        return "Status set to REJECTED";
    }

}
