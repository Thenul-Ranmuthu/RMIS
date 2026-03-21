package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.CompanyLoginResponseDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterResponseDto;
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
    public ResponseEntity<CompanyLoginResponseDto> login(@RequestBody LoginDto loginDto) {
        String token = companyAuthService.login(loginDto);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        CompanyLoginResponseDto companyLoginResponseDto = new CompanyLoginResponseDto();
        companyLoginResponseDto.setAccessToken(token);
        companyLoginResponseDto.setTokenType(jwtAuthResponse.getTokenType());
        companyLoginResponseDto.setStatus(companyAuthService.getStatusByEmail(loginDto.getEmail()));

        return new ResponseEntity<>(companyLoginResponseDto,HttpStatus.OK);
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
            
            CompanyRegisterResponseDto company = new CompanyRegisterResponseDto();
            company.setName(companyRegisterDto.getName());
            company.setCompanyid(companyRegisterDto.getCompanyid());
            company.setEmail(companyRegisterDto.getEmail());
            company.setStatus(companyAuthService.getStatusByEmail(companyRegisterDto.getEmail()));

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            company.setAccessToken(jwtAuthResponse.getAccessToken());
            company.setTokenType(jwtAuthResponse.getTokenType());

            return new ResponseEntity<>(company, HttpStatus.CREATED);
        }else{
            return new ResponseEntity<>("Invalid Verification Code!!",HttpStatus.UNAUTHORIZED);
        }

        
    }
    
    
}
