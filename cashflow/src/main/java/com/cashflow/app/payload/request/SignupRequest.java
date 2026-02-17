package com.cashflow.app.payload.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String phone;
    private String password;
    private String gender; // MALE, FEMALE
    private Integer age;
}
