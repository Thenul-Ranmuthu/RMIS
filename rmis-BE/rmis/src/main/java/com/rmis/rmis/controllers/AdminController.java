package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@AllArgsConstructor
@RequestMapping("admin")
public class AdminController {
    private AdminService adminService;

    @PutMapping("/technician/{email}")
    public ResponseEntity<String> updateTechnicianStatus(@PathVariable("email") String email) {
        String response = adminService.updateTechnicianStatus(email);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/companyQouta/{email}/{quota}")
    public ResponseEntity<String> addCompanyQouata(@PathVariable("email") String email, @PathVariable("quota") BigDecimal quota){
        String response = adminService.addCompanyQouata(email, quota);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // @GetMapping(path = "/getQuotas")
    // public ResponseEntity<List<QuotaRequestHeader>> getPendingQuotas() {
    //     List<QuotaRequestHeader> quotas = adminService.getPendingQuotas();
    //     if(quotas.isEmpty()){
    //         return new ResponseEntity<>(quotas,HttpStatus.NO_CONTENT);
    //     }
    //     return new ResponseEntity<>(quotas,HttpStatus.FOUND);
    // }

    // @PatchMapping(path = "/statusApprove/{id}")
    // public ResponseEntity<String> changeQuotaRequestStatusApprove(@PathVariable("id") Long id){
    //     String response = adminService.changeQuotaRequestStatusApprove(id);
        
    //     if(response.equalsIgnoreCase("Status set to ACCEPTED")){
    //         return new ResponseEntity<>(response,HttpStatus.OK);
    //     }

    //     return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    // }

    // @PatchMapping(path = "/statusDecline/{id}")
    // public ResponseEntity<String> changeQuotaRequestStatusDecline(@PathVariable("id") Long id){
    //     String response = adminService.changeQuotaRequestStatusDecline(id);
        
    //     if(response.equalsIgnoreCase("Status set to DECLINED")){
    //         return new ResponseEntity<>(response,HttpStatus.OK);
    //     }

    //     return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    // }
}
