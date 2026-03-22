package com.rmis.rmis.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.MinistryOfficerService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = "ministry")
public class MinistryOfficerController {

    private MinistryOfficerService ministryOfficerService;
    private EmailService emailService;
    
    @PatchMapping(path = "/statusApprove/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusApprove(@PathVariable("id") UUID id){
        String response = ministryOfficerService.changeQuotaRequestStatusApprove(id);
        
        if(response.equalsIgnoreCase("Status set to APPROVED")){
            emailService.sendNotificationRequestApproval(id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(path = "/statusReject/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusReject(@PathVariable("id") UUID id){
        String response = ministryOfficerService.changeQuotaRequestStatusReject(id);
        
        if(response.equalsIgnoreCase("Status set to REJECTED")){
            emailService.sendNotificationRequestRejection(id);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
