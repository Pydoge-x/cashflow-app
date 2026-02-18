package com.cashflow.app.service;

public interface NotificationService {
    void sendVerificationCode(String target, String code);

    boolean supports(String registrationMethod);
}
