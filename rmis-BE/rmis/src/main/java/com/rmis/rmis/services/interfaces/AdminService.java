package com.rmis.rmis.services.interfaces;

import java.math.BigDecimal;

public interface AdminService {

    String updateTechnicianStatus(String email);

    String addCompanyQouata(String email, BigDecimal quota);

    // List<QuotaRequestHeader> getPendingQuotas();

    // String changeQuotaRequestStatusApprove(Long id);

    // String changeQuotaRequestStatusDecline(Long id);
}
