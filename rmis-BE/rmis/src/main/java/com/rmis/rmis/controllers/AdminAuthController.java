package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.AdminRegisterDto;
import com.rmis.rmis.domain.dtos.JwtAuthResponse;
import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.services.interfaces.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto adminLoginRequest) {
        String token = adminAuthService.adminLogin(adminLoginRequest);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@RequestBody AdminRegisterDto adminRegisterDto) {
        String token = adminAuthService.adminRegister(adminRegisterDto);
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.CREATED);
    }
}