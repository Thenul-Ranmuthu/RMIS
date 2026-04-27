package com.rmis.rmis.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.dtos.QuotaDetailsResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.mappers.Mapper;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.CompanyService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService{

    private QuotaRequestRepository quotaRequestRepository;
    private CompanyRepository companyRepository;
    private Mapper<QuotaRequest,QuotaRequestHeaderDto> quotMapper;

    @Override
    public List<QuotaRequestHeaderDto> listQuotas(String email) {

        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("can't find company with email: " + email));

        List<QuotaRequest> qoutas = StreamSupport.stream(
                quotaRequestRepository.findAllByCompanyName(company.getName()).
                spliterator(), false)
            .collect(Collectors.toList());

        return qoutas.stream().map(quotMapper::mapTo).collect(Collectors.toList());
    }

    @Override
    public QuotaDetailsResponseDto getQuotaDetails(String email) {
        Company company = companyRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("can't find company with email: " + email));

        List<QuotaRequest> qoutas = StreamSupport.stream(
                quotaRequestRepository.findAllByCompanyName(company.getName()).
                spliterator(), false)
            .collect(Collectors.toList());

        // BigDecimal available = company.getQuota(); 
        BigDecimal used = BigDecimal.ZERO;
        
        for(QuotaRequest q: qoutas){
            if(q.getStatus() == QuotaRequestStatus.APPROVED){
                used = used.add(q.getRequestedQuota());
            }
        }

        QuotaDetailsResponseDto dto = new QuotaDetailsResponseDto();
        dto.setQuota(company.getQuota());
        dto.setRemainingQuota(company.getRemainingQuota());
        return dto;
    }

}
