package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.ServiceTicket;
import com.rmis.rmis.domain.enums.ServiceTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceTicketRepository extends JpaRepository<ServiceTicket, Long> {

    /** All tickets raised by a public user, newest first. */
    List<ServiceTicket> findByPublicUserIdOrderByCreatedAtDesc(Long publicUserId);

    /** All tickets raised by a company, newest first. */
    List<ServiceTicket> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    /** All tickets assigned to a technician, newest first. */
    List<ServiceTicket> findByTechnicianIdOrderByCreatedAtDesc(Long technicianId);

    /** Tickets for a technician filtered by status. */
    List<ServiceTicket> findByTechnicianIdAndStatusOrderByCreatedAtDesc(
            Long technicianId, ServiceTicketStatus status);

    /**
     * Double-booking guard: returns true if the slot is already referenced
     * by any existing ticket.
     */
    boolean existsByAvailabilityId(Long availabilityId);
}