package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotaRequestHeaderDto {
    private String companyEmail;
    private Long requestQuata;
}
