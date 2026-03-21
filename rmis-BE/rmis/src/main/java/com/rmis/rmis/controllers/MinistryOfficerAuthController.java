package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.JwtAuthResponse;
import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.MinistryOfficerRegisterDto;
import com.rmis.rmis.services.interfaces.MinistryOfficerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ministry/auth")
@RequiredArgsConstructor
public class MinistryOfficerAuthController {

    private final MinistryOfficerAuthService ministryOfficerAuthService;

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@RequestBody MinistryOfficerRegisterDto registerDto) {
        String token = ministryOfficerAuthService.ministryOfficerRegister(registerDto);
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginRequest) {
        String token = ministryOfficerAuthService.ministryOfficerLogin(loginRequest);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }
}

