package com.rmis.rmis;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.domain.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.services.impl.QuotaRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QuotaRequestServiceUnitTest {

    private QuotaRequestRepository quotaRequestRepository;
    private QuotaRequestServiceImpl quotaRequestService;

    @BeforeEach
    void setUp() {
        quotaRequestRepository = Mockito.mock(QuotaRequestRepository.class);
        quotaRequestService = new QuotaRequestServiceImpl(quotaRequestRepository);
    }

    @Test
    void testToDtoMapping() {
        // Given
        QuotaRequest entity = QuotaRequest.builder()
                .requestNumber(10L)
                .companyName("Test Company")
                .requestedQuota(new BigDecimal("500"))
                .submissionDate(LocalDateTime.now())
                .status(QuotaRequestStatus.PENDING)
                .build();

        // When
        // Accessing the private method indirectly or if it was protected...
        // Wait, toDto is private in the service, but let's see if I can test formatted
        // id
        // Better to test a public method that calls toDto

        // Actually, let's just use reflection or if the service has a public list call
        // getAllRequests calls toDto
        Mockito.when(quotaRequestRepository.findAll()).thenReturn(java.util.List.of(entity));

        var dtos = quotaRequestService.getAllRequests();

        // Then
        assertEquals(1, dtos.size());
        QuotaRequestResponseDto dto = dtos.get(0);
        assertEquals("REQ-0010", dto.getRequestId());
        assertEquals("Test Company", dto.getCompanyName());
        assertEquals(new BigDecimal("500"), dto.getRequestedQuota());
        assertEquals(QuotaRequestStatus.PENDING, dto.getStatus());
        assertNotNull(dto.getSubmissionDate());
    }

    @Test
    void testRequestIdFormatting() {
        // This tests the internal formatting logic via the public method result
        QuotaRequest entity = QuotaRequest.builder()
                .requestNumber(5L)
                .companyName("C")
                .requestedQuota(BigDecimal.ZERO)
                .status(QuotaRequestStatus.PENDING)
                .build();

        Mockito.when(quotaRequestRepository.findAll()).thenReturn(java.util.List.of(entity));
        var dtos = quotaRequestService.getAllRequests();

        assertEquals("REQ-0005", dtos.get(0).getRequestId());
    }
}
