package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;
import com.rmis.rmis.domain.dtos.JwtAuthResponse;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.services.impl.CodeGeneratorService;
import com.rmis.rmis.services.interfaces.CompanyAuthService;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@AllArgsConstructor
@RequestMapping("auth/company")
public class CompanyAuthController {
    private CompanyAuthService companyAuthService;

    private CodeGeneratorService codeGeneratorService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        String token = companyAuthService.login(loginDto);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse,HttpStatus.OK);
    }

    @PostMapping("/register/{code}")
    public ResponseEntity<?> register(@RequestBody CompanyRegisterDto companyRegisterDto, @PathVariable("code") String code) {
        String token;

        if(codeGeneratorService.validateCode(companyRegisterDto.getEmail(), code)){
            try {
                token = companyAuthService.register(companyRegisterDto);
            } catch (RegisterUserAlreadyExistsException e) {
                Map<String,String> errorResponse = new HashMap<>();
                errorResponse.put("error", e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            return new ResponseEntity<>(jwtAuthResponse, HttpStatus.CREATED);
        }else{
            return new ResponseEntity<>("Invalid Verification Code!!",HttpStatus.UNAUTHORIZED);
        }

        
    }
    
    
}
