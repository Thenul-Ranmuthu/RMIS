package com.rmis.rmis.domain.dtos;

import lombok.*;
import java.math.BigDecimal;


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