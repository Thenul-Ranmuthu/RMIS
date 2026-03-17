package com.rmis.rmis.services.impl;

import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService{

    private TechnicianRepository technicianRepository;
    private CompanyRepository companyRepository;

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
    public String addCompanyQouata(String email, Long quota) {
        if(!companyRepository.existsByEmail(email)){
            return "company not found in the repository!!";
        }
        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found by email: " + email));

        company.setQuota(quota);
        companyRepository.save(company);
        return "Company Quota set to: " + quota;
    }

}
