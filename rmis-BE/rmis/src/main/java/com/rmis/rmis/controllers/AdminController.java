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
}
