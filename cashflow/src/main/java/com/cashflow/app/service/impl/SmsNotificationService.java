package com.cashflow.app.service.impl;

import com.cashflow.app.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {

    @Value("${app.sms.provider:mock}")
    private String provider;

    @Value("${app.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${app.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${app.sms.sign-name:CashFlow}")
    private String signName;

    @Value("${app.sms.template-code:}")
    private String templateCode;

    @Async
    @Override
    public void sendVerificationCode(String target, String code) {
        if ("aliyun".equalsIgnoreCase(provider)) {
            sendAliyunSms(target, code);
        } else if ("twilio".equalsIgnoreCase(provider)) {
            sendTwilioSms(target, code);
        } else {
            // Mock/Fallback
            System.out.println("========================================");
            System.out.println("MOCK SMS SENT TO: " + target);
            System.out.println("VERIFICATION CODE: " + code);
            System.out.println("DEBUG: Check application.properties for real SMS configuration.");
            System.out.println("========================================");
        }
    }

    private void sendAliyunSms(String target, String code) {
        // Placeholder for Aliyun SMS SDK implementation
        // Requirements: com.aliyun:dysmsapi20170525
        System.out.println("Attempting to send Aliyun SMS to: " + target);
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            System.err.println("Aliyun credentials missing in application.properties!");
            return;
        }
        // Verification code would be sent here using the SDK
        System.out.println("Using SignName: " + signName + ", Template: " + templateCode);
    }

    private void sendTwilioSms(String target, String code) {
        // Placeholder for Twilio SMS SDK implementation
        // Requirements: com.twilio.sdk:twilio
        System.out.println("Attempting to send Twilio SMS to: " + target);
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            System.err.println("Twilio credentials missing (SID/Token)!");
            return;
        }
        // SMS would be sent here using Twilio.init(accessKeyId, accessKeySecret)
    }

    @Override
    public boolean supports(String registrationMethod) {
        return "PHONE".equalsIgnoreCase(registrationMethod);
    }
}
