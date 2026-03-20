package com.rmis.rmis.services.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.dtos.QuotaDetailsResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequestHeader;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestHeaderRepository;
import com.rmis.rmis.services.interfaces.QuotaRequestHeaderService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class QuotaRequestHeaderServiceImpl implements QuotaRequestHeaderService{
    private QuotaRequestHeaderRepository quotaRequestHeaderRepository;
    private CompanyRepository companyRepository;

    @Override
    public String addQuotaRequest(QuotaRequestHeaderDto quotaRequestHeaderDto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        if(quotaRequestHeaderDto.getRequestQuata() > company.getQuota()){
            return "Error: Insuffitient quota balance!!";
        }
        

        QuotaRequestHeader entity = new QuotaRequestHeader();

        // entity.setCompanyEmail(quotaRequestHeaderDto.getCompanyEmail());
        entity.setCompany(company);
        entity.setRequestQuata(quotaRequestHeaderDto.getRequestQuata());

        quotaRequestHeaderRepository.save(entity);
        return "Quota saved succefully!!";
        
    }

    @Override
    public QuotaDetailsResponseDto getQuotaDetails() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        QuotaDetailsResponseDto quotaDetailsResponseDto = new QuotaDetailsResponseDto();
        quotaDetailsResponseDto.setQuota(company.getQuota());

        return quotaDetailsResponseDto;
    }


}
