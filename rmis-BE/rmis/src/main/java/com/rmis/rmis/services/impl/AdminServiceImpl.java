package com.rmis.rmis.services.impl;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.Company;
// import com.rmis.rmis.domain.entities.QuotaRequestHeader;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.enums.CompanyStatus;
// import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
// import com.rmis.rmis.repositories.QuotaRequestHeaderRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService{

    private TechnicianRepository technicianRepository;
    private CompanyRepository companyRepository;
    // private QuotaRequestHeaderRepository quotaRequestHeaderRepository;

    @Override
    public String updateTechnicianStatus(String email) {
        if(!technicianRepository.existsByEmail(email)){
            return "Technician not found in the repository!!";
        }
        Technician technician = technicianRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Technician not found with email: " + email));
        
        technician.setStatus("VERIFIED");
        technicianRepository.save(technician);
        return "Technician verified successfully!!";
    }

    @Override
    public String addCompanyQouata(String email, BigDecimal quota) {
        if(!companyRepository.existsByEmail(email)){
            return "company not found in the repository!!";
        }
        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found by email: " + email));

        company.setQuota(quota);
        company.setStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company);
        return "Company Quota set to: " + quota;
    }

    // @Override
    // public List<QuotaRequestHeader> getPendingQuotas() {
    //     List<QuotaRequestHeader> headers = StreamSupport.stream(
    //         quotaRequestHeaderRepository.findAllByStatus(QuotaRequestStatus.PENDING).
    //         spliterator(),false)
    //     .collect(Collectors.toList());
    //     return headers;
    // }

    // @Override
    // public String changeQuotaRequestStatusApprove(Long id) {
    //     if(!quotaRequestHeaderRepository.existsById(id)){
    //         return "No quota found with the ID: " + id;
    //     }

    //     QuotaRequestHeader quotaRequestHeader = quotaRequestHeaderRepository.findById(id)
    //         .orElseThrow(() -> new RuntimeException("No Quota request!!"));

    //     quotaRequestHeader.setStatus(QuotaRequestStatus.ACCEPTED);
        
    //     quotaRequestHeaderRepository.save(quotaRequestHeader);
        
    //     Company company = companyRepository.findByEmail(quotaRequestHeader.getCompany().getEmail())
    //         .orElseThrow(() -> new RuntimeException("No Company found!!"));

    //     company.setQuota(company.getQuota()-quotaRequestHeader.getRequestQuata());

    //     companyRepository.save(company);

    //     return "Status set to ACCEPTED";
    // }

    // @Override
    // public String changeQuotaRequestStatusDecline(Long id) {
    //     if(!quotaRequestHeaderRepository.existsById(id)){
    //         return "No quota found with the ID: " + id;
    //     }

    //     QuotaRequestHeader quotaRequestHeader = quotaRequestHeaderRepository.findById(id)
    //         .orElseThrow(() -> new RuntimeException("No Quota request!!"));

    //     quotaRequestHeader.setStatus(QuotaRequestStatus.DECLINED);
        
    //     quotaRequestHeaderRepository.save(quotaRequestHeader);

    //     return "Status set to DECLINED";
    // }

}
