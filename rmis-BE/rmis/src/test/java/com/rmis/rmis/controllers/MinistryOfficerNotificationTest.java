package com.rmis.rmis.controllers;

import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.MinistryOfficerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MinistryOfficerNotificationTest {

    @Mock
    private MinistryOfficerService ministryOfficerService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MinistryOfficerController ministryOfficerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNotifyCompanyOnApproval_Success() {
        UUID requestId = UUID.randomUUID();
        when(ministryOfficerService.changeQuotaRequestStatusApprove(any(UUID.class)))
                .thenReturn("Status set to APPROVED");

        ResponseEntity<String> response = ministryOfficerController.changeQuotaRequestStatusApprove(requestId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Status set to APPROVED", response.getBody());
        verify(emailService, times(1)).sendNotificationRequestApproval(requestId);
    }

    @Test
    void testNotifyCompanyOnApproval_InvalidId() {
        UUID requestId = UUID.randomUUID();
        when(ministryOfficerService.changeQuotaRequestStatusApprove(any(UUID.class)))
                .thenReturn("Error: Request ID not found in the DB!!");

        ResponseEntity<String> response = ministryOfficerController.changeQuotaRequestStatusApprove(requestId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(emailService, times(0)).sendNotificationRequestApproval(any(UUID.class));
    }

    @Test
    void testNotifyCompanyOnRejection_Success() {
        UUID requestId = UUID.randomUUID();
        when(ministryOfficerService.changeQuotaRequestStatusReject(any(UUID.class)))
                .thenReturn("Status set to REJECTED");

        ResponseEntity<String> response = ministryOfficerController.changeQuotaRequestStatusReject(requestId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Status set to REJECTED", response.getBody());
        verify(emailService, times(1)).sendNotificationRequestRejection(requestId);
    }

    @Test
    void testNotifyCompanyOnRejection_InvalidId() {
        UUID requestId = UUID.randomUUID();
        when(ministryOfficerService.changeQuotaRequestStatusReject(any(UUID.class)))
                .thenReturn("Error: Request ID not found in the DB!!");

        ResponseEntity<String> response = ministryOfficerController.changeQuotaRequestStatusReject(requestId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(emailService, times(0)).sendNotificationRequestRejection(any(UUID.class));
    }
}
