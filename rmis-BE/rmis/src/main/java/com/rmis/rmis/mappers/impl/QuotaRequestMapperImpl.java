package com.rmis.rmis.mappers.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.mappers.Mapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class QuotaRequestMapperImpl implements Mapper<QuotaRequest,QuotaRequestHeaderDto>{
    
    private ModelMapper modelMapper;

    @Override
    public QuotaRequestHeaderDto mapTo(QuotaRequest a) {
        // return modelMapper.map(a,QuotaRequestHeaderDto.class);

        return QuotaRequestHeaderDto.builder()
            .requestId(a.getRequestId())
            .requestNumber(a.getRequestNumber())
            .companyName(a.getCompanyName())
            .requestedQuota(a.getRequestedQuota())
            .submissionDate(a.getSubmissionDate())
            .status(a.getStatus())
        .build();
    }

    @Override
    public QuotaRequest mapFrom(QuotaRequestHeaderDto b) {
        return modelMapper.map(b, QuotaRequest.class);
    }
    
}
