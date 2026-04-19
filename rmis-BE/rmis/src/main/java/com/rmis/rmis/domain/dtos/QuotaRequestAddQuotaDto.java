package com.rmis.rmis.domain.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotaRequestAddQuotaDto {
    @NotNull(message = "Requested quota cannot be null")
    @DecimalMin(value = "0.01", message = "Requested quota must be greater than zero")
    private BigDecimal requestedQuota;
    
    private String companyEmail;
}
