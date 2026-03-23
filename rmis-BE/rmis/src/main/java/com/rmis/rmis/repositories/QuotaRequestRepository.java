package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.QuotaRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface QuotaRequestRepository extends JpaRepository<QuotaRequest, UUID>, JpaSpecificationExecutor<QuotaRequest> {
    List<QuotaRequest> findByCompanyName(String companyName);

    List<QuotaRequest> findAllByCompanyName(String name);

    @Query("SELECT q FROM QuotaRequest q JOIN FETCH q.company WHERE q.requestId = :id")
    Optional<QuotaRequest> findByIdWithCompany(@Param("id") UUID id);
}
