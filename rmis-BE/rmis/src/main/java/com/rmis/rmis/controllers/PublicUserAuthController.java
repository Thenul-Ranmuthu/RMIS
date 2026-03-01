package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.JwtAuthResponse;
import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.PublicUserRegisterDto;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.services.impl.CodeGeneratorService;
import com.rmis.rmis.services.interfaces.PublicUserAuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("auth/user")
public class PublicUserAuthController {
    private PublicUserAuthService publicUserAuthService;
    private CodeGeneratorService codeGeneratorService;

    @RequestMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto userLoginDto) {
            String token = publicUserAuthService.publicUserLogin(userLoginDto);

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

    @RequestMapping("/register/{code}")
    public ResponseEntity<?> register(@RequestBody PublicUserRegisterDto userRegDTO, @PathVariable("code") String code) {
        String token;

        if (codeGeneratorService.validateCode(userRegDTO.getEmail(), code)) {

            try {
                token = publicUserAuthService.publicUserRegistration(userRegDTO);
            } catch (RegisterUserAlreadyExistsException e) {
                Map<String,String> errorResponse = new HashMap<>();
                errorResponse.put("error", e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setAccessToken(token);

            return new ResponseEntity<>(jwtAuthResponse, HttpStatus.CREATED);
        } else{
            return new ResponseEntity<>("Invalid Verification Code!!",HttpStatus.UNAUTHORIZED);
        }
    }

}
