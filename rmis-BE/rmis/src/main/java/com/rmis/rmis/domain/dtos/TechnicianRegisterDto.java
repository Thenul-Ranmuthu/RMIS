package com.rmis.rmis.domain.dtos;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianRegisterDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String specialization;

    // List of certification files (PDF, JPG, PNG)
    private List<MultipartFile> certificationFiles;

    // Names of certifications matching the files above
    private List<String> certificationNames;
}
