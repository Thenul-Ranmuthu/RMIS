package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterResponseDto;
import com.rmis.rmis.domain.dtos.TechnicianResponseDto;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.domain.enums.SkillLevel;
import java.time.LocalDate;
import java.util.List;

public interface TechnicianAuthService {
    public TechnicianRegisterResponseDto register(TechnicianRegisterDto dto);

    public Technician getPendingTechnicians(LoginDto loginDto);

    public String login(LoginDto loginDto);

    TechnicianResponseDto approveTechnician(Long technicianId, SkillLevel skillLevel); // added SkillLevel param
    TechnicianResponseDto rejectTechnician(Long technicianId, String reason);
    TechnicianResponseDto getTechnicianById(Long id);
    List<TechnicianResponseDto> getTechniciansByStatus(String status);
    void deleteTechnician(Long id);
    List<TechnicianResponseDto> searchAvailableTechnicians(LocalDate date, SkillLevel skillLevel); // new method
}