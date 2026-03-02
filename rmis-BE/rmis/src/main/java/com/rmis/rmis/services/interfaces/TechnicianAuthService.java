package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterDto;
import com.rmis.rmis.domain.dtos.TechnicianRegisterResponseDto;
import com.rmis.rmis.domain.entities.Technician;

public interface TechnicianAuthService {
    public TechnicianRegisterResponseDto register(TechnicianRegisterDto dto);

    public Technician getPendingTechnicians(LoginDto loginDto);

    public String login(LoginDto loginDto);
}
