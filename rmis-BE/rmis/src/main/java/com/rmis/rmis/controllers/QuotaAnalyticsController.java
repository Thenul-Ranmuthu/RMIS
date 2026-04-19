package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;
import com.rmis.rmis.services.interfaces.QuotaAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SRP: Only handles HTTP concerns — routing, status codes, response wrapping.
 * DIP: Depends on QuotaAnalyticsService interface, not the implementation.
 *
 * @PreAuthorize is a second layer of defence on top of SecurityConfig,
 * following the principle of defence-in-depth.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class QuotaAnalyticsController {

    private final QuotaAnalyticsService analyticsService;

    /**
     * GET /api/v1/analytics/dashboard
     *
     * Returns system-wide totals AND per-company breakdown in one response.
     * The frontend uses this to render both the summary cards and the bar/pie charts.
     *
     * Scenario 1: returns totalApprovedQuota, totalUsedQuota, totalRemainingQuota
     * Scenario 2: companyBreakdowns list drives the chart data series
     * Scenario 3: @PreAuthorize + SecurityConfig reject non-admin callers with 403
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsDashboardDto> getDashboard() {
        AnalyticsDashboardDto dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }
}
