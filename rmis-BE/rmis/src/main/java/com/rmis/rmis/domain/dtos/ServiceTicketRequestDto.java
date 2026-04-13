package com.rmis.rmis.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceTicketRequestDto {

    @NotNull(message = "Availability slot ID is required")
    private Long availabilityId;

    @NotBlank(message = "Service type is required")
    @Size(max = 100, message = "Service type must not exceed 100 characters")
    private String serviceType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}