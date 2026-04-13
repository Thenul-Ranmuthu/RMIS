package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertificationResponseDto {
    private Long id;
    private String certificationName;
    private String issuingAuthority;
    private String fileType;
    private String fileUrl;
    private String originalFileName;
}