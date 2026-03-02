package com.rmis.rmis.services.impl;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.rmis.rmis.services.interfaces.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.mail.resend.api}")
    private String resendApiKey;

    @Override
    public void sendPasswordResetEmail(String email, String resetLink) {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("RMIS Support <rmis.verify@rmis.space>")
                .to(email)
                .subject("Password Reset Request - RMIS")
                .html("""
                            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                                <h2 style="color: #333;">Password Reset Request</h2>
                                <p>Hello,</p>
                                <p>You requested to reset your password for your RMIS account. Click the button below to reset it:</p>
                                <div style="text-align: center; margin: 30px 0;">
                                    <a href="%s" style="background-color: #007bff; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">Reset Password</a>
                                </div>
                                <p>This link will expire in 24 hours.</p>
                                <p>If you did not request a password reset, please ignore this email.</p>
                                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                                <p style="font-size: 12px; color: #777;">RMIS Team</p>
                            </div>
                        """
                        .formatted(resetLink))
                .build();

        try {
            resend.emails().send(params);
        } catch (Exception e) {
            // In a real app, you'd log this properly
            System.err.println("Failed to send password reset email to " + email + ": " + e.getMessage());
        }
    }
}
