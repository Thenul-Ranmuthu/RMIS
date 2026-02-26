package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CertificationDto {

    @NotBlank(message = "Certification name is required")
    private String certificationName;

    @NotBlank(message = "Issuing authority is required")
    private String issuingAuthority;

    private String issueDate;

    private String expiryDate;

    @NotBlank(message = "Certificate number is required")
    private String certificateNumber;

    @NotNull(message = "Certification file is required")
    private MultipartFile file;
}
