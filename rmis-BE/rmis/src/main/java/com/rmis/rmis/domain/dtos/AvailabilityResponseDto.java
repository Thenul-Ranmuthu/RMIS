package com.rmis.rmis.domain.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilityResponseDto {

    private Long id;
    private Long technicianId;
    private String technicianName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
}