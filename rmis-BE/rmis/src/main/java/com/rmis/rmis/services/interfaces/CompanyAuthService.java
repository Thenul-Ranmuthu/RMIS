package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.enums.CompanyStatus;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;

public interface CompanyAuthService {

    String login(LoginDto loginDto);

    String register(CompanyRegisterDto companyRegisterDto);

    CompanyStatus getStatusByEmail(String email);

}
