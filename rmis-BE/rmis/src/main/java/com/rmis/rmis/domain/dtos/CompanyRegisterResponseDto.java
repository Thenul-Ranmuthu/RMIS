package com.rmis.rmis.domain.dtos;

import com.rmis.rmis.enums.CompanyStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRegisterResponseDto {
    private String name;
    private String email;
    private String companyid;
    private CompanyStatus status;
    private Double quota;
    private String accessToken;
    private String tokenType = "Bearer";

}
