package com.rmis.rmis.domain.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportReportDto {

    private LocalDateTime generatedAt;
    private SystemQuotaSummaryDto systemSummary;
    private List<CompanyRow> companyRows;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyRow {
        private String     companyName;
        private String     registrationNumber;
        private BigDecimal allocatedQuota;   // company share of annual budget
        private BigDecimal usedQuota;        // sum of approved request amounts
        private BigDecimal remainingQuota;
        private double     usagePercentage;
    }
}

