package com.rmis.rmis.services.impl;


import com.rmis.rmis.analytics.stratergy.QuotaAggregationStrategy;
import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;
import com.rmis.rmis.services.interfaces.QuotaAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SRP: Only orchestrates the strategy call and transaction boundary.
 * DIP: Depends on QuotaAggregationStrategy abstraction.
 * Facade: Single entry point for analytics — the controller calls getDashboardData()
 *         and does not know about strategies or repositories.
 */
@Service
@RequiredArgsConstructor
public class QuotaAnalyticsServiceImpl implements QuotaAnalyticsService {

    // Spring injects FullDashboardAggregationStrategy (or any other registered strategy)
    private final QuotaAggregationStrategy aggregationStrategy;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsDashboardDto getDashboardData() {
        return aggregationStrategy.aggregate();
    }
}