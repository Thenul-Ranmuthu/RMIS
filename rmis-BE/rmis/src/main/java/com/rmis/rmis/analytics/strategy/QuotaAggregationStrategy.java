package com.rmis.rmis.analytics.strategy;

import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;

public interface QuotaAggregationStrategy {
    AnalyticsDashboardDto aggregate();
}
