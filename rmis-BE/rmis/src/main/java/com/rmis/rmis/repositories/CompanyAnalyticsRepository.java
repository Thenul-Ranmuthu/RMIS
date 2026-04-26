package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompanyAnalyticsRepository extends JpaRepository<Company, Long> {
    long countByStatus(com.rmis.rmis.enums.CompanyStatus status);
}