package com.rmis.rmis.services.impl;


import com.rmis.rmis.analytics.strategy.QuotaAggregationStrategy;
import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;
import com.rmis.rmis.services.interfaces.QuotaAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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