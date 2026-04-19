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

    List<ServiceTicket> findAllByOrderByCreatedAtDesc();

    List<ServiceTicket> findByStatusOrderByCreatedAtDesc(ServiceTicketStatus status);

    boolean existsByAvailabilityId(Long availabilityId);
    boolean existsByAvailabilityIdAndStatusNot(Long availabilityId, ServiceTicketStatus status);

    boolean existsByTechnicianIdAndStatusIn(Long technicianId, List<ServiceTicketStatus> statuses);//fix

    List<ServiceTicket> findByTechnicianId(Long technicianId);//fix
}