package com.rmis.rmis.domain.dtos;
// AnalyticsDashboardDTO.java  — top-level response combining both views

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardDto {

    private SystemQuotaSummaryDto systemSummary;
    private List<CompanyQuotaBreakdownDto> companyBreakdowns;
    private LocalDateTime generatedAt;  // tells the frontend when data was computed
}