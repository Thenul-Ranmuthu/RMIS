package com.rmis.rmis.services.interfaces;

import java.util.List;

import com.rmis.rmis.domain.dtos.QuotaDetailsResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;

public interface CompanyService {

    List<QuotaRequestHeaderDto> listQuotas(String email);

    QuotaDetailsResponseDto getQuotaDetails(String email);    

}
