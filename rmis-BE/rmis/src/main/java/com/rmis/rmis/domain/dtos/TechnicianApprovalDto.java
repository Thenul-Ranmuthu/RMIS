package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianApprovalDto {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Action is required")
    private String action; // APPROVE, REJECT

    private String rejectionReason;
}