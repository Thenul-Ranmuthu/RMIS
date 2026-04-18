package com.rmis.rmis.repositories;
// QuotaRequestAnalyticsRepository.java


import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISP: Focused only on quota request queries needed by analytics.
 * Does NOT extend a generic "all quota operations" repository.
 */
public interface QuotaRequestAnalyticsRepository
        extends JpaRepository<QuotaRequest, Long> {

    /**
     * SYSTEM-WIDE used quota.
     * "Used quota" = SUM of approvedAmount on all APPROVED requests.
     * This replaces the old QuotaAllocation-based query entirely.
     */
    @Query("""
        SELECT COALESCE(SUM(qr.approvedAmount), 0)
        FROM QuotaRequest qr
        WHERE qr.status = 'APPROVED'
    """)
    BigDecimal sumApprovedUsedAmount();

    /**
     * PER-COMPANY approved/used amounts grouped by company.
     * approvedAmount here = used quota for that company.
     * Returns: [companyId, companyName, registrationNumber, sumApprovedAmount]
     */
    @Query("""
        SELECT qr.company.id,
               qr.company.name,
               qr.company.companyid,
               COALESCE(SUM(qr.approvedAmount), 0),
               qr.company.quota
        FROM QuotaRequest qr
        WHERE qr.status = 'APPROVED'
        GROUP BY qr.company.id, qr.company.name, qr.company.companyid, qr.company.quota
        ORDER BY qr.company.name ASC
    """)
    List<Object[]> findApprovedUsedAmountGroupedByCompany();

    /**
     * Count of approved requests — for the summary card subtitle.
     */
    long countByStatus(QuotaRequestStatus status);
}
