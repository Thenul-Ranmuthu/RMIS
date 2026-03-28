package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    Optional<Technician> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Technician> findByStatus(String status);
}