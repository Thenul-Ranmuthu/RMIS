package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.MinistryOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinistryOfficerRepository extends JpaRepository<MinistryOfficer, Long> {
    Optional<MinistryOfficer> findByEmail(String email);
    boolean existsByEmail(String officerEmail);
}
