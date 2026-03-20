package com.rmis.rmis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rmis.rmis.domain.entities.QuotaRequestHeader;
import com.rmis.rmis.enums.QuotaRequestStatus;

@Repository
public interface QuotaRequestHeaderRepository extends JpaRepository<QuotaRequestHeader,Long>{

    List<QuotaRequestHeader> findAllByStatus(QuotaRequestStatus pending);

}
