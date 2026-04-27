package com.rmis.rmis.services.interfaces;

import java.math.BigDecimal;
import java.util.List;

import com.rmis.rmis.domain.dtos.CompanyDetailsDto;

public interface AdminService {

    String updateTechnicianStatus(String email);

    String addCompanyQouata(String email, BigDecimal quota);

    String addYearlyQuota(BigDecimal quota);

    List<CompanyDetailsDto> getPendingCompanies();

    // List<QuotaRequestHeader> getPendingQuotas();

    // String changeQuotaRequestStatusApprove(Long id);

    // String changeQuotaRequestStatusDecline(Long id);
}
