package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRegisterDto {
    private String name;
    private String email;
    private String companyid;
    private String password;
}
