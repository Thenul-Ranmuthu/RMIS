package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface QuotaRequestService {
    List<QuotaRequestResponseDto> getAllRequests();

    @Nullable PagedResponseDto<QuotaRequestResponseDto> getQuotaRequestsPaginated(int page, int limit);
}
