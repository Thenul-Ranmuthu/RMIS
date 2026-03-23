package com.rmis.rmis.domain.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.rmis.rmis.enums.QuotaRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuotaRequestHeaderDto {
    private UUID requestId;
    private Long requestNumber;
    private String companyName;
    private BigDecimal requestedQuota;
    private LocalDateTime submissionDate;
    private QuotaRequestStatus status;
}
