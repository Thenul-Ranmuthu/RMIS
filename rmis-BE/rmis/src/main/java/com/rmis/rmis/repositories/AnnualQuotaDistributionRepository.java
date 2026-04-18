package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.AnnualQuotaDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * ISP: Focused only on annual quota queries needed by analytics.
 *
 * The ministry sets one (or more) annual quota records.
 * For analytics we sum them all — if your business rule is
 * "exactly one active record", add an 'active' flag and filter by it.
 */
public interface AnnualQuotaDistributionRepository
        extends JpaRepository<AnnualQuotaDistribution, Long> {

    /**
     * System-wide ministry budget for the year.
     * "Total approved quota" shown on the dashboard = this value.
     */
    @Query("SELECT COALESCE(SUM(a.annualQuota), 0) FROM AnnualQuotaDistribution a")
    BigDecimal sumAnnualQuota();
}
