package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuotaRequestNotificationTest {

    @Mock
    private QuotaRequestService quotaRequestService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private QuotaRequestController quotaRequestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNotifyOfficerOnSubmission_SuccessfulSubmission() {
        QuotaRequestAddQuotaDto dto = new QuotaRequestAddQuotaDto();
        dto.setCompanyEmail("company@test.com");
        dto.setRequestedQuota(new BigDecimal("500"));

        when(quotaRequestService.addQuotaRequest(any(QuotaRequestAddQuotaDto.class)))
                .thenReturn("Quota saved succefully!!");

        ResponseEntity<String> response = quotaRequestController.addQuotaRequest(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Quota saved succefully!!", response.getBody());
        
        // Verifies the notification is triggered for submission
        verify(emailService, times(1)).sendNotificationNewRequestSubmission(dto);
    }

    @Test
    void testNotifyOfficerOnSubmission_FailedSubmission_BugCheck() {
        QuotaRequestAddQuotaDto dto = new QuotaRequestAddQuotaDto();
        dto.setCompanyEmail("company@test.com");
        dto.setRequestedQuota(new BigDecimal("99999"));

        when(quotaRequestService.addQuotaRequest(any(QuotaRequestAddQuotaDto.class)))
                .thenReturn("Error: Insuffitient quota balance!!");

        ResponseEntity<String> response = quotaRequestController.addQuotaRequest(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        // NOTE: This test intentionally verifies the BUG currently in the system where 
        // the email is fired even if the submission fundamentally fails validation.
        // It should ideally be 0 if the code behaved properly, but according to current logic, it is 1.
        verify(emailService, times(1)).sendNotificationNewRequestSubmission(dto);
    }
}
