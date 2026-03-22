package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);

    void sendNotificationNewRequestSubmission(QuotaRequestAddQuotaDto quotaRequestHeaderDto);
}
