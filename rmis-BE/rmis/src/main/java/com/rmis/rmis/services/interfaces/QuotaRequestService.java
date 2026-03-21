package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.enums.QuotaRequestStatus;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface QuotaRequestService {
    List<QuotaRequestResponseDto> getAllRequests();

    @Nullable PagedResponseDto<QuotaRequestResponseDto> getQuotaRequestsPaginated(int page, int limit);

    PagedResponseDto<QuotaRequestResponseDto> getFilteredRequests(QuotaRequestStatus status,
                                                                  String companyName,
                                                                  LocalDate submissionDate,
                                                                  int page, int limit);

    String addQuotaRequest(QuotaRequestHeaderDto quotaRequestHeaderDto);
}
