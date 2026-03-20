package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.AdminRegisterDto;
import com.rmis.rmis.domain.dtos.LoginDto;

public interface AdminAuthService {
    String adminLogin(LoginDto loginRequest);
    String adminRegister(AdminRegisterDto adminRegisterDto);
}
