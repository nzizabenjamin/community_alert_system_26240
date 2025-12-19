package com.comunityalert.cas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:noreply@communityalert.com}")
    private String fromEmail;

    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOTP(String to, String otp) {
        if (emailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject("Your OTP Verification Code");
                message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in 5 minutes.\n\nIf you didn't request this code, please ignore this email.");
                
                mailSender.send(message);
                System.out.println("[EmailService] OTP email sent successfully to " + to);
            } catch (Exception e) {
                System.err.println("[EmailService] Failed to send email: " + e.getMessage());
                // Fallback to console output
                System.out.println("[EmailService] OTP CODE: " + otp + " for " + to);
            }
        } else {
            // Development mode: just print to console
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔐 OTP VERIFICATION CODE");
            System.out.println("=".repeat(60));
            System.out.println("Email: " + to);
            System.out.println("OTP Code: " + otp);
            System.out.println("Valid for: 5 minutes");
            System.out.println("=".repeat(60) + "\n");
        }
    }

    public void sendPasswordResetEmail(String to, String link) {
        if (emailEnabled && mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject("Password Reset Request");
                message.setText("You requested a password reset. Click the link below to reset your password:\n\n" + 
                              link + "\n\nThis link will expire in 1 hour.\n\nIf you didn't request this, please ignore this email.");
                
                mailSender.send(message);
                System.out.println("[EmailService] Password reset email sent successfully to " + to);
            } catch (Exception e) {
                System.err.println("[EmailService] Failed to send email: " + e.getMessage());
                // Fallback to console output
                System.out.println("[EmailService] Password reset link: " + link + " for " + to);
            }
        } else {
            // Development mode: just print to console
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔑 PASSWORD RESET LINK");
            System.out.println("=".repeat(60));
            System.out.println("Email: " + to);
            System.out.println("Reset Link: " + link);
            System.out.println("Valid for: 1 hour");
            System.out.println("=".repeat(60) + "\n");
        }
    }
}
