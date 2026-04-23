package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.AnnualQuotaDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface AnnualQuotaDistributionRepository
        extends JpaRepository<AnnualQuotaDistribution, Long> {

    @Query("SELECT COALESCE(SUM(a.annualQuota), 0) FROM AnnualQuotaDistribution a")
    BigDecimal sumAnnualQuota();
}
