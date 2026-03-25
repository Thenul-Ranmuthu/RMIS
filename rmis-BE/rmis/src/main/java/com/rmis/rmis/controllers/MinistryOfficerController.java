package com.rmis.rmis.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.services.interfaces.MinistryOfficerService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = "ministry")
public class MinistryOfficerController {

    private MinistryOfficerService ministryOfficerService;
    
    @PatchMapping(path = "/statusApprove/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusApprove(@PathVariable("id") UUID id){
        String response = ministryOfficerService.changeQuotaRequestStatusApprove(id);
        
        if(response.equalsIgnoreCase("Status set to APPROVED")){
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @PatchMapping(path = "/statusReject/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusReject(@PathVariable("id") UUID id, @RequestBody(required = false) String reason){
        String response = ministryOfficerService.changeQuotaRequestStatusReject(id, reason);
        
        if(response.equalsIgnoreCase("Status set to REJECTED")){
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
