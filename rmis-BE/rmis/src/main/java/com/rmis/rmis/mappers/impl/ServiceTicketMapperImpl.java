package com.rmis.rmis.mappers.impl;

import com.rmis.rmis.domain.dtos.ServiceTicketResponseDto;
import com.rmis.rmis.domain.entities.Availability;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.domain.entities.ServiceTicket;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.mappers.Mapper;
import com.rmis.rmis.repositories.ServiceRatingRepository;
import org.springframework.stereotype.Component;

@Component
public class ServiceTicketMapperImpl implements Mapper<ServiceTicket, ServiceTicketResponseDto> {

    private final ServiceRatingRepository serviceRatingRepository;

    public ServiceTicketMapperImpl(ServiceRatingRepository serviceRatingRepository) {
        this.serviceRatingRepository = serviceRatingRepository;
    }

    @Override
    public ServiceTicketResponseDto mapTo(ServiceTicket t) {
        ServiceTicketResponseDto.ServiceTicketResponseDtoBuilder builder = ServiceTicketResponseDto.builder()
                .id(t.getId())
                .ticketNumber(t.getTicketNumber())
                .status(t.getStatus().name())
                .serviceType(t.getServiceType())
                .description(t.getDescription())
                .cancellationReason(t.getCancellationReason())
                .cancellationTimestamp(t.getCancellationTimestamp())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .rated(t.getServiceRating() != null || serviceRatingRepository.existsByServiceTicketId(t.getId()));

        // CUSTOMER MAPPING
        if (t.getPublicUser() != null) {
            PublicUser u = t.getPublicUser();
            builder.customerName(u.getFirstName() + " " + u.getLastName())
                    .customerEmail(u.getEmail())
                    .customerType("PUBLIC_USER");
        } else if (t.getCompany() != null) {
            Company c = t.getCompany();
            builder.customerName(c.getName())
                    .customerEmail(c.getEmail())
                    .customerType("COMPANY");
        }

        // TECHNICIAN MAPPING
        Technician tech = t.getTechnician();
        if (tech != null) {
            builder.technicianId(tech.getId())
                    .technicianName(tech.getFirstName() + " " + tech.getLastName())
                    .technicianSpecialization(tech.getSpecialization());
        }

        // AVAILABILITY MAPPING
        Availability slot = t.getAvailability();
        if (slot != null) {
            builder.availabilityId(slot.getId())
                    .scheduledDate(slot.getDate())
                    .scheduledStartTime(slot.getStartTime())
                    .scheduledEndTime(slot.getEndTime());
        }

        return builder.build();
    }

    @Override
    public ServiceTicket mapFrom(ServiceTicketResponseDto dto) {
        // Simple mapping from DTO back to Entity is usually not needed for tickets
        // as they are immutable in most cases from the user side.
        throw new UnsupportedOperationException("Map from DTO to Entity not implemented yet");
    }
}
