package com.kilimo.ticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    public PasswordResetEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${spring.mail.username:noreply@ticketflow.local}")
    private String fromAddress;

    public String buildResetLink(String token) {
        return appBaseUrl + "/reset-password?token=" + token;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (mailSender == null) {
            throw new IllegalStateException("Mail sender is not configured.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("TicketFlow Password Reset");
        message.setText(
            "We received a request to reset your TicketFlow password.\n\n"
            + "Use this link to set a new password:\n"
            + resetLink + "\n\n"
            + "This link expires in 30 minutes.\n"
            + "If you did not request this reset, you can ignore this email."
        );
        mailSender.send(message);
    }
}
