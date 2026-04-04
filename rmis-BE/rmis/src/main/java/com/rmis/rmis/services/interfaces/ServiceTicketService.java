package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;

import java.util.List;

public interface ServiceTicketService {

    ServiceTicketResponseDto raiseTicketAsPublicUser(String userEmail, ServiceTicketRequestDto dto);

    ServiceTicketResponseDto raiseTicketAsCompany(String companyEmail, ServiceTicketRequestDto dto);

    List<ServiceTicketResponseDto> getMyTicketsAsPublicUser(String userEmail);

    List<ServiceTicketResponseDto> getMyTicketsAsCompany(String companyEmail);

    ServiceTicketResponseDto getTicketById(Long ticketId);
}