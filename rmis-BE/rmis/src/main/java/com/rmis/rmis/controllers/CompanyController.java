package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // System.out.println(email);
        // List<QuotaRequestHeaderDto> quotaRequestHeaderDtos = companyService.getQuotas(email);
        // if(quotaRequestHeaderDtos.isEmpty()){
        //     return new ResponseEntity<>(quotaRequestHeaderDtos,HttpStatus.NO_CONTENT);
        // }
        // return new ResponseEntity<>(quotaRequestHeaderDtos,HttpStatus.OK);
        
        List<QuotaRequestHeaderDto> dtos = companyService.listQuotas(email);

        if(dtos.isEmpty()){
            return new ResponseEntity<>(dtos,HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dtos,HttpStatus.OK);
    }
    
}
