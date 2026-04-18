package com.rmis.rmis.domain.dtos;
// AnalyticsDashboardDTO.java  — top-level response combining both views

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Composite DTO returned to the frontend for the full dashboard.
 * Supports both bar chart (per-company) and pie chart (system-wide) data.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardDto {

    private SystemQuotaSummaryDto systemSummary;
    private List<CompanyQuotaBreakdownDto> companyBreakdowns;
    private LocalDateTime generatedAt;  // tells the frontend when data was computed
}