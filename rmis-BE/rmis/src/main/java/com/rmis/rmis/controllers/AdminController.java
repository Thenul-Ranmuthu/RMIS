package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.*;

import com.rmis.rmis.domain.dtos.CompanyDetailsDto;
import com.rmis.rmis.enums.CompanyStatus;
import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/companies/pending")
    public ResponseEntity<List<CompanyDetailsDto>> getPendingCompanies() {
        List<CompanyDetailsDto> details = adminService.getPendingCompanies();

        // System.out.println("return : "+details);
        if(details.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            List<CompanyDetailsDto> pending = new ArrayList<>();
            for(CompanyDetailsDto i: details){
                if(i.getStatus().equals(CompanyStatus.PENDING)){
                    pending.add(i);
                }
            }
            return new ResponseEntity<>(pending,HttpStatus.OK);
        }
    }

    @GetMapping("/getPendingActive")
    public ResponseEntity<List<CompanyDetailsDto>> getPendingActive() {
        List<CompanyDetailsDto> details = adminService.getPendingCompanies();

        // System.out.println("return : "+details);
        if(details.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }else{
            List<CompanyDetailsDto> pending = new ArrayList<>();
            for(CompanyDetailsDto i: details){
                if(i.getStatus().equals(CompanyStatus.ACTIVE)){
                    pending.add(i);
                }
            }
            return new ResponseEntity<>(pending,HttpStatus.OK);
        }
    }
    

}
