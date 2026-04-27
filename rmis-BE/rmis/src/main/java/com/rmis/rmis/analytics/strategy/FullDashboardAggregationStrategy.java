package com.rmis.rmis.analytics.strategy;

// FullDashboardAggregationStrategy.java  — Concrete strategy: all companies, all time



import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;
import com.rmis.rmis.domain.dtos.CompanyQuotaBreakdownDto;
import com.rmis.rmis.domain.dtos.SystemQuotaSummaryDto;
import com.rmis.rmis.enums.CompanyStatus;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.AnnualQuotaDistributionRepository;
import com.rmis.rmis.repositories.CompanyAnalyticsRepository;
import com.rmis.rmis.repositories.QuotaRequestAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FullDashboardAggregationStrategy implements QuotaAggregationStrategy {

    private final QuotaRequestAnalyticsRepository requestRepo;
    private final AnnualQuotaDistributionRepository annualQuotaRepo; // replaces allocationRepo
    private final CompanyAnalyticsRepository companyRepo;

    @Override
    public AnalyticsDashboardDto aggregate() {
        SystemQuotaSummaryDto summary = buildSystemSummary();
        List<CompanyQuotaBreakdownDto> breakdowns = buildCompanyBreakdowns(summary.getTotalApprovedQuota());

        return AnalyticsDashboardDto.builder()
                .systemSummary(summary)
                .companyBreakdowns(breakdowns)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ── private helpers ──────────────────────────────────────

    private SystemQuotaSummaryDto buildSystemSummary() {
        // "Total approved quota" = ministry's annual budget from AnnualQuotaDistribution
        BigDecimal totalApproved  = annualQuotaRepo.sumAnnualQuota();

        // "Total used quota" = sum of approvedAmount on APPROVED QuotaRequests
        BigDecimal totalUsed      = requestRepo.sumApprovedUsedAmount();

        // "Total remaining" = ministry budget minus what companies have consumed
        BigDecimal totalRemaining = totalApproved.subtract(totalUsed);

        long activeCompanies   = companyRepo.countByStatus(CompanyStatus.ACTIVE);
        long approvedRequests  = requestRepo.countByStatus(QuotaRequestStatus.APPROVED);

        return SystemQuotaSummaryDto.builder()
                .totalApprovedQuota(totalApproved)
                .totalUsedQuota(totalUsed)
                .totalRemainingQuota(totalRemaining)
                .totalCompanies(activeCompanies)
                .totalApprovedRequests(approvedRequests)
                .build();
    }

    private List<CompanyQuotaBreakdownDto> buildCompanyBreakdowns(BigDecimal systemAnnualQuota) {
        /*
         * Per-company row:
         *   usedQuota     = SUM of their APPROVED request amounts
         *   approvedQuota = same value — approved request amount IS the used quota
         *   remainingQuota = their proportional slice of the annual budget minus what they've used.
         *
         * NOTE: If you later add an explicit per-company allocation column to
         * AnnualQuotaDistribution (e.g. companyAllocation), replace systemAnnualQuota
         * here with that company-specific figure to get a more precise remainingQuota.
         */
        List<Object[]> rows = requestRepo.findApprovedUsedAmountGroupedByCompany();
        // long totalCompanies = companyRepo.countByStatus(CompanyStatus.ACTIVE);


        return rows.stream()
                .map(row -> {
                    Long       companyId   = (Long)       row[0];
                    String     name        = (String)     row[1];
                    String     regNumber   = (String)     row[2];
                    BigDecimal companyShare = (BigDecimal) row[4];
                    BigDecimal usedQuota   = (BigDecimal) row[3]; // approved = used


                    BigDecimal remaining   = companyShare.subtract(usedQuota);
                    double     usagePct    = calculateUsagePercentage(companyShare, usedQuota);

                    return CompanyQuotaBreakdownDto.builder()
                            .companyId(companyId)
                            .companyName(name)
                            .registrationNumber(regNumber)
                            .approvedQuota(companyShare)   // their slice of the annual budget
                            .usedQuota(usedQuota)          // what they've actually consumed
                            .remainingQuota(remaining)
                            .usagePercentage(usagePct)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double calculateUsagePercentage(BigDecimal allocated, BigDecimal used) {
        if (allocated.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return used.multiply(BigDecimal.valueOf(100))
                .divide(allocated, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
