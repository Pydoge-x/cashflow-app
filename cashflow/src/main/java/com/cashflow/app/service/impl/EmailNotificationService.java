package com.cashflow.app.service.impl;

import com.cashflow.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendVerificationCode(String target, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(target);
        message.setSubject("CashFlow 注册验证码");
        message.setText("您的注册验证码为: " + code + "\n\n该验证码 5 分钟内有效，请勿泄露给他人。");

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + target);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + target + ": " + e.getMessage());
            // In a production app, you might want to throw a custom exception or retry
        }
    }

    @Override
    public boolean supports(String registrationMethod) {
        return "EMAIL".equalsIgnoreCase(registrationMethod);
    }
}
