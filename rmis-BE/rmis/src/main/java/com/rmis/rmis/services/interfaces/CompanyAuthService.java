package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.CompanyRegisterDto;
import com.rmis.rmis.domain.dtos.LoginDto;

public interface CompanyAuthService {

    String login(LoginDto loginDto);

    String register(CompanyRegisterDto companyRegisterDto);

}
