package com.rmis.rmis.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingStatusUpdateRequestDto {

    @NotBlank(message = "Status is required")
    private String status;

    private String cancellationReason;
}