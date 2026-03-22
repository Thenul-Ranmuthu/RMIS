package com.rmis.rmis.services.interfaces;

import java.util.UUID;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);

    void sendNotificationNewRequestSubmission(QuotaRequestAddQuotaDto quotaRequestHeaderDto);

    void sendNotificationRequestApproval(UUID id);

    void sendNotificationRequestRejection(UUID id);
}
