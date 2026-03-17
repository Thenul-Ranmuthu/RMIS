package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface QuotaRequestRepository extends JpaRepository<QuotaRequest, UUID>, JpaSpecificationExecutor<QuotaRequest> {
    List<QuotaRequest> findByCompanyName(String companyName);
}
