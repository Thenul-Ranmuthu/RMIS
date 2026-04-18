package com.rmis.rmis.repositories;
// CompanyAnalyticsRepository.java

import com.rmis.rmis.domain.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Minimal company repository scoped to analytics use-case.
 */
public interface CompanyAnalyticsRepository extends JpaRepository<Company, Long> {
    long countByStatus(com.rmis.rmis.enums.CompanyStatus status);
}