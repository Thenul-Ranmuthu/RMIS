package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.ServiceTicket;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceTicketRepository extends JpaRepository<ServiceTicket, Long> {

    List<ServiceTicket> findByPublicUserIdOrderByCreatedAtDesc(Long publicUserId);

    List<ServiceTicket> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<ServiceTicket> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    List<ServiceTicket> findByTechnicianIdAndStatusOrderByCreatedAtDesc(
            Long technicianId, ServiceTicketStatus status);

    boolean existsByAvailabilityId(Long availabilityId);
}