package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Technician;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends CrudRepository<Technician, Long> {
    Optional<Technician> findByEmail(String email);
    List<Technician> findByStatus(String status);
    boolean existsByEmail(String email);
}
