package com.rmis.rmis.services.interfaces;

public interface EmailService {
    void sendPasswordResetEmail(String email, String resetLink);
}
