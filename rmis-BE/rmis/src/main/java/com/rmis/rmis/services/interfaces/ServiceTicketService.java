package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.ServiceTicketRequestDto;
import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;
import com.rmis.rmis.domain.dtos.ServiceRatingRequestDto;
import com.rmis.rmis.domain.dtos.ServiceRatingResponseDto;

import java.util.List;

public interface ServiceTicketService {

    ServiceTicketResponseDto raiseTicketAsPublicUser(String userEmail, ServiceTicketRequestDto dto);

    ServiceTicketResponseDto raiseTicketAsCompany(String companyEmail, ServiceTicketRequestDto dto);

    List<ServiceTicketResponseDto> getMyTicketsAsPublicUser(String userEmail);

    List<ServiceTicketResponseDto> getMyTicketsAsCompany(String companyEmail);

    List<ServiceTicketResponseDto> getAllTickets();

    List<ServiceTicketResponseDto> getAllTicketsByStatus(String status);

    ServiceTicketResponseDto getTicketById(Long ticketId);

    ServiceTicketResponseDto cancelTicket(Long ticketId,  String reason, String userEmail); // Scenario 1 & 2

    ServiceRatingResponseDto submitRating(Long ticketId, String userEmail, ServiceRatingRequestDto dto);

    List<ServiceRatingResponseDto> getTechnicianFeedbacks(Long technicianId);
}