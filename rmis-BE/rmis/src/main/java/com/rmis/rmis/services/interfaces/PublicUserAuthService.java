package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.PublicUserRegisterDto;

public interface PublicUserAuthService {
    public String publicUserRegistration(PublicUserRegisterDto userDTO);
    public String publicUserLogin(PublicUserRegisterDto userDTO);
}
