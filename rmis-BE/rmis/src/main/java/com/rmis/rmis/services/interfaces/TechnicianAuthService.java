package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.*;

import java.util.List;

public interface TechnicianAuthService {
    TechnicianResponseDto register(TechnicianRegisterDto registerDto);
    String login(TechnicianLoginDto loginDto);
    TechnicianResponseDto approveTechnician(Long technicianId);
    TechnicianResponseDto rejectTechnician(Long technicianId, String reason);
    TechnicianResponseDto getTechnicianById(Long id);
    List<TechnicianResponseDto> getTechniciansByStatus(String status);
    TechnicianResponseDto updateTechnicianProfile(Long id, TechnicianRegisterDto updateDto);
    void deleteTechnician(Long id);
}