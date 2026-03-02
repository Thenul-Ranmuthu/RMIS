package com.rmis.rmis.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.JwtAuthResponse;
import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterResponseDto;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.exceptions.FileStorageException;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.services.impl.CodeGeneratorService;
import com.rmis.rmis.services.interfaces.TechnicianAuthService;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@Slf4j
@RequestMapping("/auth/technician")
@AllArgsConstructor
public class TechnicianAuthController {

    private final TechnicianAuthService technicianAuthService;
    private CodeGeneratorService codeGeneratorService;

    @PostMapping(path = "/register/{code}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> register(@ModelAttribute TechnicianRegisterDto registerDto, @PathVariable("code") String code) {
        log.info("Registration request for: {}", registerDto.getEmail());
        System.out.println("Registration request for: " + registerDto.getEmail());

        if(codeGeneratorService.validateCode(registerDto.getEmail(), code)){
            try {
                TechnicianRegisterResponseDto response = technicianAuthService.register(registerDto);

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Registration successful. Pending admin approval.");
                result.put("technician", response);

                return new ResponseEntity<>(result, HttpStatus.CREATED);

            } catch (RegisterUserAlreadyExistsException e) {
                return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.CONFLICT);
            } catch (FileStorageException e) {
                return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                log.error("Registration failed", e);
                System.out.println("Registration failed");
                return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else{
            return new ResponseEntity<>("Invalid Verification Code!!",HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {

        Technician technician = technicianAuthService.getPendingTechnicians(loginDto);
        if(technician == null){
            return new ResponseEntity<>("Technician not verified!!",HttpStatus.NOT_FOUND);
        }

        String token = technicianAuthService.login(loginDto);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);

        return new ResponseEntity<>(jwtAuthResponse,HttpStatus.OK);
    }
}
