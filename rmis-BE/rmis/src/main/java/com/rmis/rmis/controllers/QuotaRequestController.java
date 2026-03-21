package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.QuotaRequestHeaderDto;
import com.rmis.rmis.services.interfaces.QuotaRequestService;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@AllArgsConstructor
@RequestMapping("quotaHeader")
public class QuotaRequestController {

    private QuotaRequestService quotaRequestService;

    @PostMapping(path = "/addQuota")
    public ResponseEntity<String> addQuotaRequest(@RequestBody QuotaRequestHeaderDto quotaRequestHeaderDto) {
        String response = quotaRequestService.addQuotaRequest(quotaRequestHeaderDto);
        if(response.equalsIgnoreCase("Error: Insuffitient quota balance!!")){
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    
    
}
