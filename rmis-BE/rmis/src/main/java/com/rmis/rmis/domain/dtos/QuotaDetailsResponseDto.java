package com.rmis.rmis.domain.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotaDetailsResponseDto {
    private BigDecimal quota;
    private BigDecimal remainingQuota;
}