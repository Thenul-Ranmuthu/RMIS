package com.rmis.rmis.mappers.impl;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.mappers.Mapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class QuotaRequestResponseMapperImpl implements Mapper<QuotaRequest, QuotaRequestResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public QuotaRequestResponseDto mapTo(QuotaRequest entity) {
        return QuotaRequestResponseDto.builder()
                .id(entity.getRequestId())
                .requestId(String.valueOf(entity.getRequestNumber()))
                .companyName(entity.getCompanyName())
                .requestedQuota(entity.getRequestedQuota())
                .submissionDate(entity.getSubmissionDate())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public QuotaRequest mapFrom(QuotaRequestResponseDto dto) {
        return modelMapper.map(dto, QuotaRequest.class);
    }
}
