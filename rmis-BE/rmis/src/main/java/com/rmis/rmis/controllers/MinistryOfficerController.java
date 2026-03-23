package com.rmis.rmis.controllers;

import java.util.UUID;

import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.services.interfaces.MinistryOfficerService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/ministry/quota-requests")
public class MinistryOfficerController {

    private MinistryOfficerService ministryOfficerService;
    private MinistryOfficerRepository ministryOfficerRepository;
    
    @PatchMapping(path = "/statusApprove/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusApprove(@PathVariable("id") UUID id){
        // Get the logged-in officer's email from the security context
        String officerEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Load the full officer object from DB using their email
        MinistryOfficer officer = ministryOfficerRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new RuntimeException("Officer not found"));
        String response = ministryOfficerService.changeQuotaRequestStatusApprove(id, officer);
        
        if(response.equalsIgnoreCase("Status set to APPROVED")){
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }

    @PatchMapping(path = "/statusReject/{id}")
    public ResponseEntity<String> changeQuotaRequestStatusReject(@PathVariable("id") UUID id){
        // Get the logged-in officer's email from the security context
        String officerEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Load the full officer object from DB using their email
        MinistryOfficer officer = ministryOfficerRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        String response = ministryOfficerService.changeQuotaRequestStatusReject(id, officer);
        
        if(response.equalsIgnoreCase("Status set to REJECTED")){
            return new ResponseEntity<>(response,HttpStatus.OK);
        }

        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
