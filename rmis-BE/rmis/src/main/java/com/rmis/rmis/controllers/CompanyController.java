package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.QuotaDetailsResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.services.interfaces.CompanyService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping(path = "company")
@AllArgsConstructor
public class CompanyController {
    private CompanyService companyService;

    @GetMapping("/getQuotas")
    public ResponseEntity<List<QuotaRequestHeaderDto>> getQuotas() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        List<QuotaRequestHeaderDto> dtos = companyService.listQuotas(email);

        //if(dtos.isEmpty()){
        //    return new ResponseEntity<>(dtos,HttpStatus.NO_CONTENT);
        //}
        //return new ResponseEntity<>(dtos,HttpStatus.OK);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/getQuotaDetails")
    public ResponseEntity<QuotaDetailsResponseDto> getQuotaDetails() {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        QuotaDetailsResponseDto dto = companyService.getQuotaDetails(email);
        return new ResponseEntity<>(dto,HttpStatus.OK);
    }
    
    
}
