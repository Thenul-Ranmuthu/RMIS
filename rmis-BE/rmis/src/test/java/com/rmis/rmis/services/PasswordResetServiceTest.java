package com.rmis.rmis.services;

import com.rmis.rmis.domain.dtos.PasswordResetRequestDto;
import com.rmis.rmis.domain.dtos.ResetPasswordDto;
import com.rmis.rmis.domain.entities.PasswordResetToken;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.exceptions.InvalidTokenException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.exceptions.WeakPasswordException;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.PasswordResetTokenRepository;
import com.rmis.rmis.repositories.PublicUserRepository;
import com.rmis.rmis.services.impl.PasswordResetServiceImpl;
import com.rmis.rmis.services.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PublicUserRepository publicUserRepo;

    @Mock
    private CompanyRepository companyRepo;

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestPasswordReset_Success_PublicUser() {
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto("test@example.com");
        when(publicUserRepo.existsByEmail("test@example.com")).thenReturn(true);

        passwordResetService.requestPasswordReset(requestDto);

        verify(tokenRepo).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void requestPasswordReset_Failure_UnregisteredEmail() {
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto("unknown@example.com");
        when(publicUserRepo.existsByEmail("unknown@example.com")).thenReturn(false);
        when(companyRepo.existsByEmail("unknown@example.com")).thenReturn(false);

        assertThrows(UnregisteredUserException.class, () -> passwordResetService.requestPasswordReset(requestDto));
    }

    @Test
    void resetPassword_Success() {
        ResetPasswordDto resetDto = new ResetPasswordDto("valid-token", "NewPass123!");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setEmail("test@example.com");
        token.setAccountType("PUBLIC");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        PublicUser user = new PublicUser();
        user.setEmail("test@example.com");

        when(tokenRepo.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(publicUserRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("hashed-password");

        passwordResetService.resetPassword(resetDto);

        assertEquals("hashed-password", user.getPassword());
        verify(publicUserRepo).save(user);
        verify(tokenRepo).delete(token);
    }

    @Test
    void resetPassword_Failure_WeakPassword() {
        ResetPasswordDto resetDto = new ResetPasswordDto("valid-token", "weak");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(tokenRepo.findByToken("valid-token")).thenReturn(Optional.of(token));

        assertThrows(WeakPasswordException.class, () -> passwordResetService.resetPassword(resetDto));
    }

    @Test
    void resetPassword_Failure_ExpiredToken() {
        ResetPasswordDto resetDto = new ResetPasswordDto("expired-token", "NewPass123!");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setExpiryDate(LocalDateTime.now().minusHours(1));

        when(tokenRepo.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(resetDto));
    }
}
