package com.rmis.rmis.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rmis.rmis.domain.entities.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>{
    Optional<Company> findByEmail(String email);

    boolean existsByEmail(String email);
} 
