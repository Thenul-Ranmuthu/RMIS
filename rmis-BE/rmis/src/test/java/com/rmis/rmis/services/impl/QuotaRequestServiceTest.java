package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuotaRequestServiceTest {

    @Mock
    private QuotaRequestRepository quotaRequestRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private QuotaRequestServiceImpl quotaRequestService;

    private static final String MOCK_EMAIL = "test@company.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void addQuotaRequest_Success() {
        // Arrange
        BigDecimal availableQuota = new BigDecimal("1000.00");
        BigDecimal requestedQuota = new BigDecimal("500.00");

        QuotaRequestHeaderDto dto = new QuotaRequestHeaderDto();
        dto.setRequestedQuota(requestedQuota);

        Company company = new Company();
        company.setName("Test Company");
        company.setEmail(MOCK_EMAIL);
        company.setQuota(availableQuota);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(MOCK_EMAIL);
        when(companyRepository.findByEmail(MOCK_EMAIL)).thenReturn(Optional.of(company));
        when(quotaRequestRepository.count()).thenReturn(10L);

        // Act
        String result = quotaRequestService.addQuotaRequest(dto);

        // Assert
        assertEquals("Quota saved succefully!!", result);
        verify(quotaRequestRepository, times(1)).save(any(QuotaRequest.class));
    }

    @Test
    void addQuotaRequest_InsufficientQuota() {
        // Arrange
        BigDecimal availableQuota = new BigDecimal("100.00");
        BigDecimal requestedQuota = new BigDecimal("500.00");

        QuotaRequestHeaderDto dto = new QuotaRequestHeaderDto();
        dto.setRequestedQuota(requestedQuota);

        Company company = new Company();
        company.setName("Small Company");
        company.setEmail(MOCK_EMAIL);
        company.setQuota(availableQuota);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(MOCK_EMAIL);
        when(companyRepository.findByEmail(MOCK_EMAIL)).thenReturn(Optional.of(company));

        // Act
        String result = quotaRequestService.addQuotaRequest(dto);

        // Assert
        assertEquals("Error: Insuffitient quota balance!!", result);
        verify(quotaRequestRepository, never()).save(any(QuotaRequest.class));
    }

    @Test
    void addQuotaRequest_CompanyNotFound() {
        // Arrange
        QuotaRequestHeaderDto dto = new QuotaRequestHeaderDto();
        dto.setRequestedQuota(new BigDecimal("100.00"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@company.com");
        when(companyRepository.findByEmail("unknown@company.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            quotaRequestService.addQuotaRequest(dto);
        });

        assertTrue(exception.getMessage().contains("Company not found"));
        verify(quotaRequestRepository, never()).save(any(QuotaRequest.class));
    }
}
