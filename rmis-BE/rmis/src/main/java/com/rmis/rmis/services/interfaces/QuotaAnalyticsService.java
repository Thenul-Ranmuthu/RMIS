package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;

/**
 * DIP: Controller depends on this interface, not the implementation.
 * Facade: Hides the strategy selection and aggregation complexity from the controller.
 */
public interface QuotaAnalyticsService {
    AnalyticsDashboardDto getDashboardData();
}
