package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.MinistryOfficerRegisterDto;

public interface MinistryOfficerAuthService {
    String ministryOfficerLogin(LoginDto loginRequest);
    String ministryOfficerRegister(MinistryOfficerRegisterDto ministryOfficerRegisterDto);
}
