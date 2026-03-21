package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinistryOfficerRegisterDto {
    private String officerName;
    private String officerEmail;
    private String officerPassword;
    private String officerId;
}
