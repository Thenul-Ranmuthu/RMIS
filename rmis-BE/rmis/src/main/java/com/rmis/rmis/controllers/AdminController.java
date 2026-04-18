package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.*;

import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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

    @PutMapping("/addApprovedQuota/{quota}")
    public ResponseEntity<String> addApprovedQuota(@PathVariable("quota")BigDecimal quota){
        String response = adminService.addYearlyQuota(quota);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
