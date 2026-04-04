package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;

import java.util.List;

public interface ServiceTicketService {

    /** Raise a ticket as an individual (public) user. */
    ServiceTicketResponseDto raiseTicketAsPublicUser(String userEmail, ServiceTicketRequestDto dto);

    /** Raise a ticket as a company user. */
    ServiceTicketResponseDto raiseTicketAsCompany(String companyEmail, ServiceTicketRequestDto dto);

    /** All tickets belonging to the authenticated public user. */
    List<ServiceTicketResponseDto> getMyTicketsAsPublicUser(String userEmail);

    /** All tickets belonging to the authenticated company. */
    List<ServiceTicketResponseDto> getMyTicketsAsCompany(String companyEmail);

    /** Fetch a single ticket by ID. */
    ServiceTicketResponseDto getTicketById(Long ticketId);
}