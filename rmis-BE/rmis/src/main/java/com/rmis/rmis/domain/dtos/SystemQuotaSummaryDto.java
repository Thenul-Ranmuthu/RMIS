package com.rmis.rmis.domain.dtos;
// SystemQuotaSummaryDTO.java  — system-wide totals

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO carrying system-wide aggregated quota figures.
 * Builder pattern used for clean, readable construction.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemQuotaSummaryDto {

    private BigDecimal totalApprovedQuota;   // SUM of approved QuotaRequest.approvedAmount
    private BigDecimal totalUsedQuota;       // SUM of QuotaAllocation.usedAmount
    private BigDecimal totalRemainingQuota;  // totalApprovedQuota - totalUsedQuota
    private long totalCompanies;
    private long totalApprovedRequests;
}