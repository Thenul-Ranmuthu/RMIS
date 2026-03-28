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
        
        technician.setStatus("ACTIVE");
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

}
