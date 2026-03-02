package com.rmis.rmis.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rmis.rmis.domain.entities.Technician;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long>{

    Optional<Technician> findByEmail(String email);

    boolean existsByEmail(String email);

}
