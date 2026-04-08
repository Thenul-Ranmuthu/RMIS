package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.domain.dtos.QuotaRequestAddQuotaDto;
import com.rmis.rmis.services.interfaces.EmailService;
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
    private EmailService emailService;

    @PostMapping(path = "/addQuota")
    public ResponseEntity<String> addQuotaRequest(@RequestBody QuotaRequestAddQuotaDto quotaRequestAddQuotaDto) {
        String response = quotaRequestService.addQuotaRequest(quotaRequestAddQuotaDto); // duplicate check runs first
        if (response.toLowerCase().startsWith("error:")) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        emailService.sendNotificationNewRequestSubmission(quotaRequestAddQuotaDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
}
