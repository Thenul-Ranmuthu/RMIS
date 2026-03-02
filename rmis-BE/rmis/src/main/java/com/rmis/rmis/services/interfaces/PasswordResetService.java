package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.PasswordResetRequestDto;
import com.rmis.rmis.domain.dtos.ResetPasswordDto;

public interface PasswordResetService {
    void requestPasswordReset(PasswordResetRequestDto requestDto);

    boolean validateResetToken(String token);

    void resetPassword(ResetPasswordDto resetDto);
}
