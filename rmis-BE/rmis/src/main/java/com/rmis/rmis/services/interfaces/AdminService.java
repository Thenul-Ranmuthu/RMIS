package com.rmis.rmis.services.interfaces;


public interface AdminService {

    String updateTechnicianStatus(String email);

    String addCompanyQouata(String email, Double quota);

    // List<QuotaRequestHeader> getPendingQuotas();

    String changeQuotaRequestStatusApprove(Long id);

    String changeQuotaRequestStatusDecline(Long id);
}
