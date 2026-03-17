package com.rmis.rmis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rmis.rmis.domain.entities.QuotaRequestHeader;

@Repository
public interface QuotaRequestHeaderRepository extends JpaRepository<QuotaRequestHeader,Long>{

}
