package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.AvailabilityRequestDto;
import com.rmis.rmis.domain.dtos.AvailabilityResponseDto;

import java.util.List;

public interface AvailabilityService {

    AvailabilityResponseDto addAvailability(String technicianEmail, AvailabilityRequestDto dto);

    List<AvailabilityResponseDto> getFutureAvailabilities(String technicianEmail);

    AvailabilityResponseDto updateAvailability(String technicianEmail, Long availabilityId, AvailabilityRequestDto dto);

    void deleteAvailability(String technicianEmail, Long availabilityId);
}