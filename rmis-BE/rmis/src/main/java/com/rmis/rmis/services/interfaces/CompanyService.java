package com.rmis.rmis.services.interfaces;

import java.util.List;

import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;

public interface CompanyService {

    // List<QuotaRequestHeaderDto> getQuotas(String email);

    List<QuotaRequestHeaderDto> listQuotas(String email);

}
