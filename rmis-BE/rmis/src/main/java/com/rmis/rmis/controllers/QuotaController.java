package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.QuotaDetailsResponseDto;
import com.rmis.rmis.services.interfaces.QuotaRequestHeaderService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@AllArgsConstructor
@RequestMapping("quota")
public class QuotaController {

    private QuotaRequestHeaderService quotaRequestHeaderService;

    @GetMapping("/getDetailse")
    public ResponseEntity<QuotaDetailsResponseDto> getQuotaDetails() {
        QuotaDetailsResponseDto quotaDetailsResponseDto = quotaRequestHeaderService.getQuotaDetails();
        return new ResponseEntity<>(quotaDetailsResponseDto,HttpStatus.OK);
    }
    
}
