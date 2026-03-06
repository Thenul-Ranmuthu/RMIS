package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;

import java.util.List;

public interface QuotaRequestService {
    public List<QuotaRequestResponseDto> getAllRequests();
}
