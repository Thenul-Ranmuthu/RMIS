package com.rmis.rmis.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@AllArgsConstructor
@RequestMapping("admin")
public class AdminController {
    private AdminService adminService;

    @PutMapping("/technician/{email}")
    public ResponseEntity<?> updateTechnicianStatus(@PathVariable("email") String email) {
        String response = adminService.updateTechnicianStatus(email);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
