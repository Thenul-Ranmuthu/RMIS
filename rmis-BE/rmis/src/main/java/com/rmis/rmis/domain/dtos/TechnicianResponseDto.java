package com.rmis.rmis.domain.dtos;

import com.rmis.rmis.domain.enums.SkillLevel;
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
    private String district;
    private String specialization;
    private Integer yearsOfExperience;
    private SkillLevel skillLevel;
    private String status;
    private LocalDateTime registrationDate;
    private LocalDateTime approvalDate;
    private List<CertificationResponseDto> certifications;
    private List<AvailabilityResponseDto> availabilities;
}