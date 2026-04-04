package com.rmis.rmis.services.interfaces;
import com.rmis.rmis.domain.dtos.TechnicianBookingResponseDto;
import java.util.List;
public interface TechnicianBookingService {
    List<TechnicianBookingResponseDto> getMyBookings(String technicianEmail);
    List<TechnicianBookingResponseDto> getMyBookingsByStatus(String
                                                                     technicianEmail, String status);
    TechnicianBookingResponseDto getBookingDetail(String technicianEmail,
                                                  Long ticketId);
}
