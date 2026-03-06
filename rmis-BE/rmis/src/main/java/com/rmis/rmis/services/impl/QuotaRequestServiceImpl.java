package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuotaRequestServiceImpl implements QuotaRequestService {
    private final QuotaRequestRepository quotaRequestRepository;

    public QuotaRequestServiceImpl(QuotaRequestRepository quotaRequestRepository) {
        this.quotaRequestRepository = quotaRequestRepository;
    }

    @Override
    public List<QuotaRequestResponseDto> getAllRequests(){
        return quotaRequestRepository.findAll()
                .stream()
                .map(entity -> new QuotaRequestResponseDto(
                        entity.getRequestId(),
                        entity.getCompanyName(),
                        entity.getRequestedQuota(),
                        entity.getSubmissionDate(),
                        entity.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
