package com.rmis.rmis.services.interfaces;

import java.util.UUID;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.domain.entities.ServiceTicket;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);

    void sendNotificationNewRequestSubmission(QuotaRequestAddQuotaDto quotaRequestHeaderDto);

    void sendNotificationRequestApproval(UUID id);

    void sendNotificationRequestRejection(UUID id);

    void sendBookingStatusUpdateEmail(ServiceTicket ticket);

    void sendBookingCancellationEmail(ServiceTicket ticket);

    void sendBookingCancellationTechnicianEmail(ServiceTicket ticket);

    void sendBookingConfirmationEmail(ServiceTicket ticket);
}
