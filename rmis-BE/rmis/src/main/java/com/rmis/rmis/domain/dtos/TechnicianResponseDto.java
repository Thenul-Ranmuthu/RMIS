package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String specialization;
    private Integer yearsOfExperience;
    private String status;
    private LocalDateTime registrationDate;
    private LocalDateTime approvalDate;
    private List<CertificationResponseDto> certifications;
}