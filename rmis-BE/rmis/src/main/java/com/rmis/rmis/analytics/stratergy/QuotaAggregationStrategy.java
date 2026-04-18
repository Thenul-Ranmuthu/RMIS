package com.rmis.rmis.analytics.stratergy;

import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;

public interface QuotaAggregationStrategy {
    AnalyticsDashboardDto aggregate();
}
