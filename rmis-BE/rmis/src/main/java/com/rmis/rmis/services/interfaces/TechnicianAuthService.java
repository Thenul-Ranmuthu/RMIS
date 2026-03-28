package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterResponseDto;
import com.rmis.rmis.domain.dtos.TechnicianResponseDto;
import com.rmis.rmis.domain.entities.Technician;

import java.util.List;

public interface TechnicianAuthService {
    public TechnicianRegisterResponseDto register(TechnicianRegisterDto dto);

    public Technician getPendingTechnicians(LoginDto loginDto);

    public String login(LoginDto loginDto);

    TechnicianResponseDto approveTechnician(Long technicianId);
    TechnicianResponseDto rejectTechnician(Long technicianId, String reason);
    TechnicianResponseDto getTechnicianById(Long id);
    List<TechnicianResponseDto> getTechniciansByStatus(String status);
    void deleteTechnician(Long id);
}
