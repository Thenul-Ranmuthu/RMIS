package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.PasswordResetRequestDto;
import com.rmis.rmis.domain.dtos.ResetPasswordDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.PasswordResetToken;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.exceptions.InvalidTokenException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.exceptions.WeakPasswordException;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.PasswordResetTokenRepository;
import com.rmis.rmis.repositories.PublicUserRepository;
import com.rmis.rmis.services.interfaces.EmailService;
import com.rmis.rmis.services.interfaces.PasswordResetService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PublicUserRepository publicUserRepo;
    private final CompanyRepository companyRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Password regex: at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1
    // special char
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    public PasswordResetServiceImpl(
            PublicUserRepository publicUserRepo,
            CompanyRepository companyRepo,
            PasswordResetTokenRepository tokenRepo,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.publicUserRepo = publicUserRepo;
        this.companyRepo = companyRepo;
        this.tokenRepo = tokenRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDto requestDto) {
        String email = requestDto.getEmail();
        String accountType;

        if (publicUserRepo.existsByEmail(email)) {
            accountType = "PUBLIC";
        } else if (companyRepo.existsByEmail(email)) {
            accountType = "COMPANY";
        } else {
            throw new UnregisteredUserException("Invalid email");
        }

        // Clean up any existing tokens for this email
        tokenRepo.deleteByEmail(email);

        // Generate token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setAccountType(accountType);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepo.save(resetToken);

        // Send email
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    @Override
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepo.findByToken(token);
        return resetToken.isPresent() && !resetToken.get().isExpired();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDto resetDto) {
        // Find token
        PasswordResetToken resetToken = tokenRepo.findByToken(resetDto.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepo.delete(resetToken);
            throw new InvalidTokenException("Reset token has expired");
        }

        // Validate password strength
        if (!PATTERN.matcher(resetDto.getNewPassword()).matches()) {
            throw new WeakPasswordException(
                    "Password must be at least 8 characters long and include uppercase, lowercase, digit, and special character.");
        }

        // Update password based on account type
        if ("PUBLIC".equals(resetToken.getAccountType())) {
            PublicUser user = publicUserRepo.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new UnregisteredUserException("User no longer exists"));
            user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
            publicUserRepo.save(user);
        } else {
            Company company = companyRepo.findByEmail(resetToken.getEmail())
                    .orElseThrow(() -> new UnregisteredUserException("Company no longer exists"));
            company.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
            companyRepo.save(company);
        }

        // Invalidate token
        tokenRepo.delete(resetToken);
    }
}
