package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.services.interfaces.QuotaRequestService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@AllArgsConstructor
@RequestMapping("quotaHeader")
public class QuotaRequestController {

    private QuotaRequestService quotaRequestService;

    @PreAuthorize("hasRole('COMPANY')")
    @PostMapping(path = "/addQuota")
    public ResponseEntity<String> addQuotaRequest(@Valid @RequestBody QuotaRequestAddQuotaDto quotaRequestAddQuotaDto) {
        String response = quotaRequestService.addQuotaRequest(quotaRequestAddQuotaDto); 
        if (response.toLowerCase().startsWith("error:")) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
}
