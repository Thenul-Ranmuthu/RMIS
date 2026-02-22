package com.rmis.rmis.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.rmis.rmis.services.impl.CodeGeneratorService;

// import lombok.AllArgsConstructor;

@RestController
// @AllArgsConstructor
public class EmailController {
    
    @Value("${app.mail.resend.api}")
    private String resendApiKey;

    private CodeGeneratorService codeGeneratorService;

    public EmailController(CodeGeneratorService codeGeneratorService) {
        this.codeGeneratorService = codeGeneratorService;
    }

    @RequestMapping(path = "/sendMail/{email}")
    public String sendEmail(@PathVariable("email") String email){
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
            .from("Acme <onboarding@resend.dev>")
            .to(email)
            .subject("RMIS verification code")
            .html("""
                <h1>Welcome to RMIS!</h1>
                <p>Thank you for registering.</p>
                </br>
                <p>Verification code: %s</p>
            """.formatted(codeGeneratorService.generateCode(email, 5)))
            .build();
        
        try {
            resend.emails().send(params);
            return "Success!!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    




}
