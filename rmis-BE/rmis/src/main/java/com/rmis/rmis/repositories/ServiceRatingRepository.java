package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.ServiceRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRatingRepository extends JpaRepository<ServiceRating, Long> {

    List<ServiceRating> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    Optional<ServiceRating> findByServiceTicketId(Long serviceTicketId);

    boolean existsByServiceTicketId(Long serviceTicketId);
}
