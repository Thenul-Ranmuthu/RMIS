package com.rmis.rmis.controllers;

import com.rmis.rmis.domain.dtos.PasswordResetRequestDto;
import com.rmis.rmis.domain.dtos.ResetPasswordDto;
import com.rmis.rmis.services.interfaces.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody PasswordResetRequestDto requestDto) {
        passwordResetService.requestPasswordReset(requestDto);
        return ResponseEntity.ok(Map.of("message", "link sent"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        if (isValid) {
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("valid", false, "message", "Invalid or expired token"));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetDto) {
        passwordResetService.resetPassword(resetDto);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
