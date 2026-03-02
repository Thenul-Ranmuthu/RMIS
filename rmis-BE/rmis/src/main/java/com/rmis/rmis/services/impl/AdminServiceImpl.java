package com.rmis.rmis.services.impl;

import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.TechnicianRepository;
import com.rmis.rmis.services.interfaces.AdminService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminServiceImpl implements AdminService{

    private TechnicianRepository technicianRepository;

    @Override
    public String updateTechnicianStatus(String email) {
        if(!technicianRepository.existsByEmail(email)){
            return "Technician not found!!";
        }
        Technician technician = technicianRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Technician not found with email: " + email));
        
        technician.setStatus("VERIFIED");
        technicianRepository.save(technician);
        return "Technician verified successfully!!";
    }

}
