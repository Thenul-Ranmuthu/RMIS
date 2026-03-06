package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.entities.QuotaRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuotaRequestRepository extends JpaRepository<QuotaRequest, UUID> {
}
