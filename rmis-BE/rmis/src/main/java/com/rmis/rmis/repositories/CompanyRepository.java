package com.rmis.rmis.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.rmis.rmis.domain.entities.Company;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long>{
    Optional<Company> findByEmail(String email);
} 
