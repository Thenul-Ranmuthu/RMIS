package com.rmis.rmis.domain.dtos;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}