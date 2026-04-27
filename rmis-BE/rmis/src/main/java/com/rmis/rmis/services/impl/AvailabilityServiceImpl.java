package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.AvailabilityRequestDto;
import com.rmis.rmis.domain.dtos.AvailabilityResponseDto;
import com.rmis.rmis.domain.entities.Availability;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.ResourceNotFoundException;
import com.rmis.rmis.repositories.AvailabilityRepository;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.AvailabilityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final TechnicianRepository technicianRepository;

    @Override
    @Transactional
    public AvailabilityResponseDto addAvailability(String technicianEmail, AvailabilityRequestDto dto) {
        Technician technician = getTechnicianByEmail(technicianEmail);

        validateTimeRange(dto);
        checkOverlap(technician.getId(), dto, null);

        Availability availability = new Availability();
        availability.setTechnician(technician);
        availability.setDate(dto.getDate());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());

        Availability saved = availabilityRepository.save(availability);
        return toResponseDto(saved);
    }

    @Override
    public List<AvailabilityResponseDto> getFutureAvailabilities(String technicianEmail) {
        Technician technician = getTechnicianByEmail(technicianEmail);

        return availabilityRepository
                .findByTechnicianIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
                        technician.getId(), LocalDate.now())
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AvailabilityResponseDto updateAvailability(String technicianEmail, Long availabilityId, AvailabilityRequestDto dto) {
        Technician technician = getTechnicianByEmail(technicianEmail);

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));

        if (!availability.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("Access denied: this slot does not belong to you");
        }

        validateTimeRange(dto);
        checkOverlap(technician.getId(), dto, availabilityId);

        availability.setDate(dto.getDate());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());

        return toResponseDto(availabilityRepository.save(availability));
    }

    @Override
    @Transactional
    public void deleteAvailability(String technicianEmail, Long availabilityId) {
        Technician technician = getTechnicianByEmail(technicianEmail);

        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));

        if (!availability.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("Access denied: this slot does not belong to you");
        }

        availabilityRepository.delete(availability);
    }

    // ---- helpers ----

    private Technician getTechnicianByEmail(String email) {
        return technicianRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));
    }

    private void validateTimeRange(AvailabilityRequestDto dto) {
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void checkOverlap(Long technicianId, AvailabilityRequestDto dto, Long excludeId) {
        boolean overlap = availabilityRepository
                .existsByTechnicianIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        technicianId, dto.getDate(), dto.getEndTime(), dto.getStartTime());

        if (overlap) {
            // If updating, allow the slot with excludeId — refetch and compare
            if (excludeId != null) {
                // Availability existing = availabilityRepository.findById(excludeId).orElse(null);
                // If the overlap is only with itself, it's fine — but our query includes it,
                // so we re-check by looking for any OTHER overlapping slot.
                // Simple approach: fetch future slots and check manually
                boolean otherOverlap = availabilityRepository
                        .findByTechnicianIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
                                technicianId, dto.getDate())
                        .stream()
                        .filter(a -> !a.getId().equals(excludeId))
                        .filter(a -> a.getDate().equals(dto.getDate()))
                        .anyMatch(a -> dto.getStartTime().isBefore(a.getEndTime())
                                && dto.getEndTime().isAfter(a.getStartTime()));

                if (otherOverlap) {
                    throw new IllegalArgumentException("This time slot overlaps with an existing availability");
                }
            } else {
                throw new IllegalArgumentException("This time slot overlaps with an existing availability");
            }
        }
    }

    private AvailabilityResponseDto toResponseDto(Availability a) {
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setId(a.getId());
        dto.setTechnicianId(a.getTechnician().getId());
        dto.setTechnicianName(a.getTechnician().getFirstName() + " " + a.getTechnician().getLastName());
        dto.setDate(a.getDate());
        dto.setStartTime(a.getStartTime());
        dto.setEndTime(a.getEndTime());
        dto.setStatus(a.getStatus());
        return dto;
    }
}