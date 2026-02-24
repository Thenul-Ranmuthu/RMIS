package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserRegisterDto {
    private String fname;
    private String lname;
    private String email;
    private String password;
    private String phone;
}
