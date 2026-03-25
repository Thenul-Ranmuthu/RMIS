package com.rmis.rmis.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.impl.MinistryOfficerServiceImpl;
import com.rmis.rmis.services.interfaces.AuditLogService;

@ExtendWith(MockitoExtension.class)
public class MinistryOfficerServiceImplTest {

    @Mock
    private QuotaRequestRepository quotaRequestRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private MinistryOfficerRepository ministryOfficerRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private MinistryOfficerServiceImpl ministryOfficerService;

    private QuotaRequest pendingRequest;
    private Company company;
    private MinistryOfficer officer;

    @BeforeEach
    void setUp() {
        // Setup Security Context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("officer@rmis.com", "password"));
        SecurityContextHolder.setContext(context);

        company = new Company();
        company.setEmail("company@test.com");
        company.setQuota(new BigDecimal("1000.00"));

        officer = new MinistryOfficer();
        officer.setEmail("officer@rmis.com");
        officer.setName("Test Officer");

        pendingRequest = new QuotaRequest();
        pendingRequest.setRequestId(UUID.randomUUID());
        pendingRequest.setStatus(QuotaRequestStatus.PENDING);
        pendingRequest.setRequestedQuota(new BigDecimal("100.00"));
        pendingRequest.setCompany(company);
        pendingRequest.setCompanyName(company.getName());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testApproveRequest_Success() {
        // Scenario 1: Approve Request
        when(quotaRequestRepository.findById(pendingRequest.getRequestId())).thenReturn(Optional.of(pendingRequest));
        when(ministryOfficerRepository.findByEmail("officer@rmis.com")).thenReturn(Optional.of(officer));
        when(companyRepository.findByEmail("company@test.com")).thenReturn(Optional.of(company));

        String result = ministryOfficerService.changeQuotaRequestStatusApprove(pendingRequest.getRequestId());

        assertEquals("Status set to APPROVED", result);
        
        // Assert state changes
        assertEquals(QuotaRequestStatus.APPROVED, pendingRequest.getStatus());
        assertEquals(officer, pendingRequest.getReviewedBy());
        assertNotNull(pendingRequest.getReviewedAt());
        
        // Assert quota deduction
        assertEquals(new BigDecimal("900.00"), company.getQuota());

        // Verify saves and audit log
        verify(quotaRequestRepository).save(pendingRequest);
        verify(companyRepository).save(company);
        verify(auditLogService).logApproval(officer, pendingRequest);
    }

    @Test
    void testRejectRequest_Success() {
        // Scenario 2: Reject Request
        when(quotaRequestRepository.findById(pendingRequest.getRequestId())).thenReturn(Optional.of(pendingRequest));
        when(ministryOfficerRepository.findByEmail("officer@rmis.com")).thenReturn(Optional.of(officer));

        String reason = "Insufficient documentation provided.";
        String result = ministryOfficerService.changeQuotaRequestStatusReject(pendingRequest.getRequestId(), reason);

        assertEquals("Status set to REJECTED", result);

        // Assert state changes
        assertEquals(QuotaRequestStatus.REJECTED, pendingRequest.getStatus());
        assertEquals(officer, pendingRequest.getReviewedBy());
        assertNotNull(pendingRequest.getReviewedAt());
        assertEquals(reason, pendingRequest.getRejectionReason());

        // Assert company quota is not deducted
        assertEquals(new BigDecimal("1000.00"), company.getQuota());

        // Verify save and audit log
        verify(quotaRequestRepository).save(pendingRequest);
        verify(auditLogService).logRejection(officer, pendingRequest, reason);
        verifyNoInteractions(companyRepository);
    }

    @Test
    void testApproveRequest_StateEnforcement_Failure() {
        // Scenario 3: State Enforcement (Already Approved)
        pendingRequest.setStatus(QuotaRequestStatus.APPROVED);
        when(quotaRequestRepository.findById(pendingRequest.getRequestId())).thenReturn(Optional.of(pendingRequest));

        String result = ministryOfficerService.changeQuotaRequestStatusApprove(pendingRequest.getRequestId());

        assertTrue(result.contains("Error: Request is already processed"));
        verifyNoInteractions(ministryOfficerRepository);
        verifyNoInteractions(companyRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void testRejectRequest_StateEnforcement_Failure() {
        // Scenario 3: State Enforcement (Already Rejected)
        pendingRequest.setStatus(QuotaRequestStatus.REJECTED);
        when(quotaRequestRepository.findById(pendingRequest.getRequestId())).thenReturn(Optional.of(pendingRequest));

        String result = ministryOfficerService.changeQuotaRequestStatusReject(pendingRequest.getRequestId(), "test");

        assertTrue(result.contains("Error: Request is already processed"));
        verifyNoInteractions(ministryOfficerRepository);
        verifyNoInteractions(companyRepository);
        verifyNoInteractions(auditLogService);
    }
}
