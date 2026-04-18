package com.rmis.rmis.domain.dtos;
// CompanyQuotaBreakdownDTO.java  — per-company figures

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for per-company quota breakdown used in chart series.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyQuotaBreakdownDto {

    private Long       companyId;
    private String     companyName;
    private String     registrationNumber;

    /**
     * The company's share of the annual ministry quota budget.
     * (Currently: annualQuota / numberOfActiveCompanies)
     * Later: replace with an explicit per-company allocation when
     * AnnualQuotaDistribution gains a company FK or allocation table.
     */
    private BigDecimal approvedQuota;

    /**
     * Sum of approvedAmount on all APPROVED QuotaRequests for this company.
     * Approved request amount = used quota amount (per domain rules).
     */
    private BigDecimal usedQuota;

    private BigDecimal remainingQuota;   // approvedQuota - usedQuota
    private double     usagePercentage;  // for pie/bar chart sizing
}