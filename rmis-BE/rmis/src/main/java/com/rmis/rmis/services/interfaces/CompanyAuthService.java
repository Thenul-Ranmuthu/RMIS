package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.CompanyLoginDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;

public interface CompanyAuthService {

    String login(CompanyLoginDto companyLoginDto);

    String register(CompanyRegisterDto companyRegisterDto);

}
