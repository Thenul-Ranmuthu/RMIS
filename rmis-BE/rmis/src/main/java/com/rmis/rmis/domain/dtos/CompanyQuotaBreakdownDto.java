package com.rmis.rmis.domain.dtos;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyQuotaBreakdownDto {

    private Long       companyId;
    private String     companyName;
    private String     registrationNumber;
    private BigDecimal approvedQuota;
    private BigDecimal usedQuota;
    private BigDecimal remainingQuota;   // approvedQuota - usedQuota
    private double     usagePercentage;  // for pie/bar chart sizing
}